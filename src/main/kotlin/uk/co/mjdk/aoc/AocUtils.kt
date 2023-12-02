package uk.co.mjdk.aoc

import java.io.BufferedReader
import java.nio.charset.StandardCharsets




fun Int.format(digits: Int) = "%0${digits}d".format(this)

fun <T> Sequence<T>.splitBy(shouldSplit: (T) -> Boolean): Sequence<List<T>> {
    return sequence {
        val buffer = mutableListOf<T>()
        for (current in this@splitBy) {
            if (shouldSplit(current)) {
                yield(buffer.toList())
                buffer.clear()
            } else {
                buffer.add(current)
            }
        }
        if (buffer.isNotEmpty()) {
            yield(buffer)
        }
    }
}

fun <T> Collection<T>.repeatForever(): Sequence<T> = sequence {
    while(true) {
        yieldAll(this@repeatForever)
    }
}
