package uk.co.mjdk.aoc23

import uk.co.mjdk.aoc.aoc

import kotlin.math.pow

private data class Card(val id: Int, val winningNumbers: Set<Int>, val yourNumbers: Set<Int>) {
    val yourWinningNumbers: Set<Int>
        get() = winningNumbers.intersect(yourNumbers)

    val points: Int
        get() = when (val numWinning = yourWinningNumbers.size) {
            0 -> 0
            else -> 2.0.pow(numWinning - 1).toInt()
        }

    companion object {
        fun parse(input: String): Card {
            val (crd, win, my) = input.split(": ", " | ")
            val id = crd.drop("Card ".length).trim().toInt()
            fun (String).toNumberSet(): Set<Int> =
                split(Regex("\\s+")).mapNotNull { it.takeUnless { it.isBlank() }?.toInt() }.toSet()
            return Card(
                id,
                win.toNumberSet(),
                my.toNumberSet(),
            )
        }
    }
}

fun main() = aoc(2023, 4) {
    part1 { input ->
        input.lineSequence().map { Card.parse(it) }.sumOf { it.points }
    }
}
