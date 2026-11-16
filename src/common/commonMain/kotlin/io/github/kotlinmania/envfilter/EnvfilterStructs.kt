package io.github.kotlinmania.envfilter

import io.github.kotlinmania.rustlang.core.mem.TypeLayout

public open class ReadmeDoctests {
    public companion object {
        public fun typeLayout(): io.github.kotlinmania.rustlang.core.mem.TypeLayout<ReadmeDoctests> =
            object : io.github.kotlinmania.rustlang.core.mem.TypeLayout<ReadmeDoctests> {
                override val alignBytes: Long = 1L
                override val sizeBytes: Long = 0L
                override fun readNative(addr: Long): ReadmeDoctests =
                    ReadmeDoctests()
                override fun writeNative(addr: Long, value: ReadmeDoctests) {
                }
            }
    }
}
