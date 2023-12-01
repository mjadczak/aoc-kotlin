package uk.co.mjdk.aoc22.day06

import uk.co.mjdk.aoc.aocInputStored

fun firstDistinctPosition(input: String, numDistinct: Int): Int =
    input.asSequence().windowed(numDistinct).withIndex().first {
        it.value.toSet().size == numDistinct
    }.index + numDistinct


fun main() {
    val input = aocInputStored(22, 6).use { it.readText() }.trim()
    println(firstDistinctPosition(input, 4))
    println(firstDistinctPosition(input, 14))
}
