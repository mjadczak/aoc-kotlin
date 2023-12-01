package uk.co.mjdk.aoc23

import uk.co.mjdk.aoc.aoc

val input = aoc(2023, 1)

fun part1() {
    input.lineSequence().sumOf { str ->
        str.filter { it.isDigit() }.map { it.digitToInt() }.let { it.first() * 10 + it.last() }
    }.let(::println)
}

fun part2() {

}

fun main() {
    part1()
    part2()
}
