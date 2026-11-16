// port-lint: source filter.rs
package io.github.kotlinmania.envfilter.filter

import io.github.kotlinmania.envfilter.directive.Directive
import io.github.kotlinmania.envfilter.directive.enabled
import io.github.kotlinmania.envfilter.op.FilterOp
import io.github.kotlinmania.envfilter.parser.parseSpec
import io.github.kotlinmania.log.LevelFilter
import io.github.kotlinmania.log.Metadata
import io.github.kotlinmania.log.Record

public class DebugStruct(
    private val out: StringBuilder,
    private val name: String,
) {
    private val fields: MutableList<String> = mutableListOf()

    public fun field(name: String, value: Any?): DebugStruct =
        apply {
            fields.add("$name: $value")
        }

    public fun finish() {
        if (fields.isEmpty()) {
            out.append(name)
        } else {
            out.append("$name { ${fields.joinToString(", ")} }")
        }
    }

    public fun finishNonExhaustive() {
        fields.add("..")
        finish()
    }
}

/**
 * A builder for a log filter.
 *
 * It can be used to parse a set of directives from a string before building
 * a [Filter] instance.
 *
 * ## Example
 *
 * ```kotlin
 * import envfilter.Builder
 *
 * var builder = Builder.new()
 * // Parse a logging filter from an environment variable.
 * when (val matchVal = variable("RUST_LOG")) {
 *     is Ok -> {
 *         val (rustLog) = matchVal
 *         builder.parse(rustLog)
 *     }
 *     else -> {}
 * }
 * val filter = builder.build()
 * ```
 */
public open class Builder(
    public var directives: MutableList<Directive>,
    public var filter: FilterOp?,
    public var built: Boolean,
) {
    /**
     * Insert the directive replacing any directive with the same name.
     */
    public open fun insertDirective(directive: Directive) {
        when (val matchVal = this.directives.indexOfFirst({ d -> d.name == directive.name }).takeIf { it >= 0 }) {
            null -> {
                this.directives.add(directive)
            }
            else -> {
                val pos = matchVal
                mem.swap(this.directives[pos], directive)
            }
        }
    }

    /**
     * Adds a directive to the filter for a specific module.
     */
    public open fun filterModule(module: String, level: LevelFilter): Builder = this.filter(module, level)

    /**
     * Adds a directive to the filter for all modules.
     */
    public open fun filterLevel(level: LevelFilter): Builder = this.filter(null, level)

    /**
     * Adds a directive to the filter.
     *
     * The given module (if any) will log at most the specified level provided.
     * If no module is provided then the filter will apply to all log messages.
     */
    public open fun filter(module: String?, level: LevelFilter): Builder {
        this.insertDirective(Directive(name = module?.let({ s -> s.toOwned() }), level = level))
        return this
    }

    /**
     * Parses the directives string.
     *
     * See the [Enabling Logging] section for more details.
     *
     * [Enabling Logging]: ../index.html#enabling-logging
     */
    public open fun parse(filters: String): Builder {
        // compatibility
        val structVal = parseSpec(filters)
        val directives = structVal.directives
        val filter = structVal.filter
        val errors = structVal.errors
        for (error in errors) {
            println("warning: $error, ignoring it")
        }
        this.filter = filter
        for (directive in directives) {
            this.insertDirective(directive)
        }
        return this
    }

    /**
     * Parses the directive string, returning an error if the given directive string is invalid.
     *
     * See the [Enabling Logging] section for more details.
     *
     * [Enabling Logging]: ../index.html#enabling-logging
     */
    public open fun tryParse(filters: String): Result<Builder> {
        val (directives, filter) = parseSpec(filters).ok().getOrElse { return Result.failure(it) }
        this.filter = filter
        for (directive in directives) {
            this.insertDirective(directive)
        }
        return Result.success(this)
    }

    /**
     * Build a log filter.
     */
    public open fun build(): Filter {
        check(!this.built) { "attempt to re-use consumed builder" }
        this.built = true
        var directives = mutableListOf()
        if (this.directives.isEmpty()) {
            // Adds the default filter if none exist
            directives.add(Directive(name = null, level = LevelFilter.Error))
        } else {
            // Consume directives.
            directives = mem.take(this.directives)
            // Sort the directives by length of their name, this allows a
            // little more efficient lookup at runtime.
            directives.sortBy({ a, b ->
                val alen = a.name.asRef().map({ a1 -> a1.len() }).unwrapOr(0)
                val blen = b.name.asRef().map({ b1 -> b1.len() }).unwrapOr(0)
                alen.cmp(blen)
            })
        }
        return Filter(directives = mem.take(directives), filter = mem.take(this.filter))
    }

    public open fun fmt(f: StringBuilder) {
        if (this.built) {
            DebugStruct(f, "Filter").field("built", true).finish()
        } else {
            DebugStruct(f, "Filter").field("filter", this.filter).field("directives", this.directives).finish()
        }
    }

    public companion object {
        /**
         * Initializes the filter builder with defaults.
         */
        public fun new(): Builder = Builder(directives = mutableListOf(), filter = null, built = false)

        /**
         * Initializes the filter builder from an environment.
         */
        public fun fromEnv(env: String): Builder {
            val builder = Builder.new()
            when (val matchVal = variable(env)) {
                is Ok -> {
                    val (s) = matchVal
                    builder.parse(s)
                }
                else -> {}
            }
            return builder
        }

        public fun default(): Builder = Builder.new()
    }
}

/**
 * A log filter.
 *
 * This struct can be used to determine whether or not a log record
 * should be written to the output.
 * Use the [Builder] type to parse and construct a `Filter`.
 *
 * [Builder]: struct.Builder.html
 */
public open class Filter(
    public var directives: List<Directive>,
    public var filter: FilterOp?,
) {
    /**
     * Returns the maximum `LevelFilter` that this filter instance is
     * configured to output.
     *
     * # Example
     *
     * ```kotlin
     * import envfilter.Builder
     * import log.LevelFilter
     *
     * var builder = Builder.new()
     * builder.filter("module1", LevelFilter.Info)
     * builder.filter("module2", LevelFilter.Error)
     * val filter = builder.build()
     * check(filter.filter() == LevelFilter.Info)
     * ```
     */
    public open fun filter(): LevelFilter = this.directives.map({ d -> d.level }).max().unwrapOr(LevelFilter.Off)

    /**
     * Checks if this record matches the configured filter.
     */
    public open fun matches(record: Record): Boolean {
        if (!this.enabled(record.metadata())) {
            return false
        }
        when (val matchVal = this.filter) {
            null -> {}
            else -> {
                val filter = matchVal
                if (!filter.isMatch(record.args().toString())) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Determines if a log message with the specified metadata would be logged.
     */
    public open fun enabled(metadata: Metadata): Boolean {
        val level = metadata.level()
        val target = metadata.target()
        return enabled(this.directives, level, target)
    }

    public open fun fmt(f: StringBuilder) {
        DebugStruct(f, "Filter").field("filter", this.filter).field("directives", this.directives).finish()
    }
}
