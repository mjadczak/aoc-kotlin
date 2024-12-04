package uk.co.mjdk.aoc24.day02

import uk.co.mjdk.aoc.aoc
import kotlin.math.absoluteValue
import kotlin.math.sign

fun main() = aoc(2024, 2, { it.lines().map { line -> line.split(" ").map { num -> num.toInt() } } }) {
    part1 { lists ->
        lists.count { list ->
            val diffs = list.zipWithNext { a, b -> b - a }
            val signsSame = diffs.map { it.sign }.distinct().size == 1
            val sizesOk = diffs.all { it.absoluteValue in 1..3 }
            signsSame && sizesOk
        }
    }
}
