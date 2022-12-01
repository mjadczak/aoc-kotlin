package uk.co.mjdk.aoc

import java.io.BufferedReader
import java.nio.charset.StandardCharsets


fun Int.format(digits: Int) = "%0${digits}d".format(this)
fun aocInput(year: Int, day: Int): BufferedReader =
    object {}::class.java.getResourceAsStream("/uk/co/mjdk/aoc$year/input${day.format(2)}.txt")!!
        .bufferedReader(StandardCharsets.UTF_8)

fun <T> Sequence<T>.splitBy(shouldSplit: (T) -> Boolean): Sequence<List<T>> {
    val underlying = this
    return sequence {
        val buffer = mutableListOf<T>()
        for (current in underlying) {
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
