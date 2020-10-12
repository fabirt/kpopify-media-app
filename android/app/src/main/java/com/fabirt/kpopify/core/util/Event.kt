package com.fabirt.kpopify.core.util

open class Event<out T>(private val data: T) {
    var hasBeenHandled: Boolean = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            data
        }
    }

    fun peekContent() = data
}