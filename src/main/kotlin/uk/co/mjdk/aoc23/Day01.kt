package uk.co.mjdk.aoc23

import uk.co.mjdk.aoc.aoc

private val input = aoc(2023, 1)

private fun part1() {
    input.lineSequence().sumOf { str ->
        str.filter { it.isDigit() }.map { it.digitToInt() }.let { it.first() * 10 + it.last() }
    }.let(::println)
}

// https://stackoverflow.com/a/17971681
private val pat = Regex("""(?=(one|two|three|four|five|six|seven|eight|nine|\d)).""")

private fun (String).matchedDigit(): Int = when (this) {
    "one" -> 1
    "two" -> 2
    "three" -> 3
    "four" -> 4
    "five" -> 5
    "six" -> 6
    "seven" -> 7
    "eight" -> 8
    "nine" -> 9
    else -> toInt().also { check(it < 10) }
}

private fun part2() {
    input.lineSequence().sumOf { str ->
        pat.findAll(str).map { it.groupValues[1].matchedDigit() }.let { it.first() * 10 + it.last() }
    }.let(::println)
}

private fun main() {
    part1()
    part2()
}
