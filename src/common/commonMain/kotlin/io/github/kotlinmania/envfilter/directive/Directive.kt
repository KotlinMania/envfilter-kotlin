// port-lint: source directive.rs
package io.github.kotlinmania.envfilter.directive

import io.github.kotlinmania.log.Level
import io.github.kotlinmania.log.LevelFilter

internal open class Directive(
    internal var name: String?,
    internal var level: LevelFilter,
)

// Check whether a level and target are enabled by the set of directives.
internal fun enabled(directives: List<Directive>, level: Level, target: String): Boolean {
    // Search for the longest match, the vector is assumed to be pre-sorted.
    for (directive in directives.iter().rev()) {
        run {
            val matchVal = directive.name
            when {
                matchVal != null && !target.startsWith(matchVal) -> {
                    val name = matchVal
                }
                matchVal != null -> { return level <= directive.level }
                else -> { return level <= directive.level }
            }
        }
    }
    return false
}
