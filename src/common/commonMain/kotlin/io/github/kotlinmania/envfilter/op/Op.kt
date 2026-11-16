// port-lint: source op.rs
package io.github.kotlinmania.envfilter.op

import io.github.kotlinmania.regex.Regex

internal open class FilterOp(
    public var inner: Regex,
) {
    internal open fun isMatch(s: String): Boolean = this.inner.isMatch(s)

    public open fun fmt(f: StringBuilder) {
        this.inner.fmt(f)
    }

    public companion object {
        internal fun new(spec: String): Result<FilterOp> =
            run {
                val matchVal = Regex.new(spec)
                if (matchVal.isSuccess) {
                    val r = matchVal.getOrThrow()
                    Result.success(FilterOp(inner = r))
                } else {
                    val e = matchVal.exceptionOrNull()!!
                    Result.failure(e.toString())
                }
            }
    }
}
