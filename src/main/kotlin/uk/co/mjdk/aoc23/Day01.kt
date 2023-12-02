package uk.co.mjdk.aoc23

import uk.co.mjdk.aoc.aoc

fun main() = aoc(2023, 1) {
    // https://stackoverflow.com/a/17971681
    val pat = Regex("""(?=(one|two|three|four|five|six|seven|eight|nine|\d)).""")

    fun (String).matchedDigit(): Int = when (this) {
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

    fun (String).answer(lineToDigits: (String) -> Iterable<Int>) = this.lineSequence().sumOf { str ->
        lineToDigits(str).let { it.first() * 10 + it.last() }
    }

    part1 { input -> input.answer { str -> str.filter { it.isDigit() }.map { it.digitToInt() } } }
    part2 { input -> input.answer { str -> pat.findAll(str).map { it.groupValues[1].matchedDigit() }.toList() } }
}
