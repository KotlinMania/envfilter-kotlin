// port-lint: source parser.rs
package io.github.kotlinmania.envfilter.parser

import io.github.kotlinmania.envfilter.directive.Directive
import io.github.kotlinmania.envfilter.op.FilterOp
import io.github.kotlinmania.log.LevelFilter

internal open class ParseResult(
    internal var directives: MutableList<Directive>,
    internal var filter: FilterOp?,
    internal var errors: MutableList<String>,
) {
    public open fun addDirective(directive: Directive) {
        this.directives.add(directive)
    }

    public open fun setFilter(filter: FilterOp) {
        this.filter = filter
    }

    public open fun addError(message: String) {
        this.errors.add(message)
    }

    internal open fun ok(): Result<Pair<List<Directive>, FilterOp?>> {
        val directives = this.directives
        val filter = this.filter
        val errors = this.errors
        return when (val matchVal = ArrayDeque(errors).next()) {
            null -> {
                Result.success(Pair(directives, filter))
            }
            else -> {
                val error = matchVal
                Result.failure(ParseError(details = error))
            }
        }
    }
}

/**
 * Error during logger directive parsing process.
 */
public open class ParseError(
    public var details: String,
) {
    public open fun fmt(f: Formatter) {
        f.append("error parsing logger filter: ${this.details}")
    }
}

/**
 * Parse a logging specification string (e.g: `crate1, crate2.mod3, crate3.x = error / foo`)
 * and return a vector with log directives.
 */
internal fun parseSpec(spec: String): ParseResult {
    val result = ParseResult.default()
    val parts = spec.split('/')
    val mods = parts.next()
    val filter = parts.next()
    if (parts.next() != null) {
        result.addError("invalid logging spec '$spec' (too many '/'s)")
        return result
    }
    if (mods != null) {
        val m = mods
        for (s in m.split(',').map({ ss -> ss.trim() })) {
            if (s.isEmpty()) {
                continue
            }
            val parts1 = s.split('=')
            val (logLevel, name) =
                run {
                    val matchVal = Triple(parts1.next(), parts1.next()?.let({ s1 -> s1.trim() }), parts1.next())
                    when {
                        matchVal.first != null && matchVal.second == null && matchVal.third == null -> {
                            val part0 = matchVal.first
                            // if the single argument is a log-level string or number,
                            // treat that as a global fallback
                            run {
                                val matchVal1 = part0.parse()
                                if (matchVal1.isSuccess) {
                                    val num = matchVal1.getOrThrow()
                                    Pair(num, null)
                                } else {
                                    Pair(LevelFilter.max(), part0)
                                }
                            }
                        }
                        matchVal.first != null && matchVal.second != null && matchVal.second == "" && matchVal.third == null -> {
                            val part01 = matchVal.first
                            Pair(LevelFilter.max(), part01)
                        }
                        matchVal.first != null && matchVal.second != null && matchVal.third == null -> {
                            val part02 = matchVal.first
                            val part1 = matchVal.second
                            when (val matchVal2 = part1.parse()) {
                                is Ok -> {
                                    val (num1) = matchVal2
                                    Pair(num1, part02)
                                }
                                else -> {
                                    result.addError("invalid logging spec '$part1'")
                                    continue
                                }
                            }
                        }
                        else -> {
                            result.addError("invalid logging spec '$s'")
                            continue
                        }
                    }
                }
            result.addDirective(Directive(name = name.map({ s1 -> s1.toOwned() }), level = logLevel))
        }
    }
    if (filter != null) {
        val filter1 = filter
        run {
            val matchVal3 = FilterOp.new(filter1)
            if (matchVal3.isSuccess) {
                val filterOp = matchVal3.getOrThrow()
                result.setFilter(filterOp)
            } else {
                val err = matchVal3.exceptionOrNull()!!
                result.addError("invalid regex filter - $err")
            }
        }
    }
    return result
}
