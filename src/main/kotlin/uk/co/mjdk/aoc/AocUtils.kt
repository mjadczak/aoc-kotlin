package uk.co.mjdk.aoc

import java.io.BufferedReader
import java.nio.charset.StandardCharsets

object AocUtils {
    fun Int.format(digits: Int) = "%0${digits}d".format(this)
    fun aocInput(year: Int, day: Int): BufferedReader =
        this::class.java.getResourceAsStream("/uk/co/mjdk/aoc$year/input${day.format(2)}.txt")!!
            .bufferedReader(StandardCharsets.UTF_8)
}
