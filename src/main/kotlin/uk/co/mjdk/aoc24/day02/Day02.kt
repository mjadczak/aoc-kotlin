package uk.co.mjdk.aoc24.day02

import uk.co.mjdk.aoc.aoc
import kotlin.math.absoluteValue
import kotlin.math.sign

fun isValid(list: List<Int>): Boolean {
    val diffs = list.zipWithNext { a, b -> b - a }
    val signsSame = diffs.map { it.sign }.distinct().size == 1
    val sizesOk = diffs.all { it.absoluteValue in 1..3 }
    return signsSame && sizesOk
}

fun main() = aoc(2024, 2, { it.lines().map { line -> line.split(" ").map { num -> num.toInt() } } }) {
    part1 { lists ->
        lists.count { list ->
            isValid(list)
        }
    }

    part2 { lists ->
        // bruteforce - there's definitely a smarter way!
        lists.count { list ->
            isValid(list) || list.indices.any { badIdx -> isValid(list.filterIndexed { index, _ -> index != badIdx }) }
        }
    }
}
