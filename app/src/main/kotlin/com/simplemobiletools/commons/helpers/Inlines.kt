package com.simplemobiletools.commons.helpers

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long) = this.map { selector(it) }.sum()

