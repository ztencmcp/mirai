/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

public class ExceptionCollector {
    @Volatile
    private var last: Throwable? = null

    @Synchronized
    public fun collect(e: Throwable?) {
        if (e == null) return
        val last = last
        if (last != null) {
            e.addSuppressed(last)
        }
        this.last = e
    }

    /**
     * Alias to [collect] to be used inside [withExceptionCollector]
     */
    public fun collectException(e: Throwable?): Unit = collect(e)

    public fun getLast(): Throwable? = last

    @TerminalOperation // to give it a color for a clearer control flow
    public fun collectThrow(exception: Throwable): Nothing {
        collect(exception)
        throw getLast()!!
    }

    @DslMarker
    private annotation class TerminalOperation
}

/**
 * Run with a coverage of `throw`. All thrown exceptions will be caught and rethrown with [ExceptionCollector.collectThrow]
 */
public inline fun <R> withExceptionCollector(action: ExceptionCollector.() -> R): R {
    ExceptionCollector().run {
        try {
            return action()
        } catch (e: Throwable) {
            collectThrow(e)
        }
    }
}