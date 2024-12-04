package uk.co.mjdk.aoc24.day01


import uk.co.mjdk.aoc.aoc
import kotlin.math.abs

fun main() = aoc(2024, 1, { input ->
    input.lineSequence().map {
        val (l1, l2) = it.split("   ")
        l1.toInt() to l2.toInt()
    }.unzip()
}) {

    part1 { (l1, l2) ->
        l1.sorted().zip(l2.sorted()).sumOf { (a, b) -> abs(a - b) }
    }

    part2 { (l1, l2) ->
        val nums = l2.groupingBy { it }.eachCount()
        l1.sumOf { it * (nums[it] ?: 0) }
    }
}
