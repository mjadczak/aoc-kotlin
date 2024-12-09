package uk.co.mjdk.aoc24.day03

import uk.co.mjdk.aoc.aoc

val mulpat = Regex("""mul\((\d+),(\d+)\)""")

fun main() = aoc(2024, 3) {
    part1 { input ->
        mulpat.findAll(input).sumOf { mr ->
            val (x, y) = mr.destructured
            x.toInt() * y.toInt()
        }
    }
}
