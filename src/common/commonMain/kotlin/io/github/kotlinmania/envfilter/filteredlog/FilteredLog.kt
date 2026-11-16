// port-lint: source filtered_log.rs
package io.github.kotlinmania.envfilter.filteredlog

import io.github.kotlinmania.envfilter.filter.Filter
import io.github.kotlinmania.log.Log
import io.github.kotlinmania.log.Metadata
import io.github.kotlinmania.log.Record

/**
 * Decorate a [log.Log] with record [Filter]ing.
 *
 * Records that match the filter will be forwarded to the wrapped log.
 * Other records will be ignored.
 */
public open class FilteredLog<T>(
    public var log: T,
    public var filter: Filter,
) : Log {
    /**
     * Determines if a log message with the specified metadata would be logged.
     *
     * For the wrapped log, this returns `true` only if both the filter and the wrapped log return `true`.
     */
    public open fun enabled(metadata: Metadata): Boolean = this.filter.enabled(metadata) && this.log.enabled(metadata)

    /**
     * Logs the record.
     *
     * Forwards the record to the wrapped log, but only if the record matches the filter.
     */
    public open fun log(record: Record) {
        if (this.filter.matches(record)) {
            this.log.log(record)
        }
    }

    /**
     * Flushes any buffered records.
     *
     * Forwards directly to the wrapped log.
     */
    public open fun flush() {
        this.log.flush()
    }

    public companion object {
        /**
         * Create a new filtered log.
         */
        public fun <T> new(log: T, filter: Filter): FilteredLog<T> = FilteredLog(log = log, filter = filter)
    }
}
