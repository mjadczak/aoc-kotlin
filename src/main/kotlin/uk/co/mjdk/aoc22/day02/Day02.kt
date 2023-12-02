package uk.co.mjdk.aoc22.day02

import uk.co.mjdk.aoc.aocReader

enum class Result {
    Win,
    Draw,
    Loss;
}

enum class Sign {
    Rock,
    Paper,
    Scissors;

    fun battle(other: Sign): Result {
        if (this == other) {
            return Result.Draw;
        }

        return when (this to other) {
            Rock to Scissors, Scissors to Paper, Paper to Rock -> Result.Win
            else -> Result.Loss
        }
    }
}

fun main() {
    // Part 1
    aocReader(22, 2).useLines { lines ->
        lines
            .map {
                assert(it.length == 3)
                val theirPlay = when (it[0]) {
                    'A' -> Sign.Rock
                    'B' -> Sign.Paper
                    'C' -> Sign.Scissors
                    else -> throw IllegalArgumentException()
                }

                val ourPlay = when (it[2]) {
                    'X' -> Sign.Rock
                    'Y' -> Sign.Paper
                    'Z' -> Sign.Scissors
                    else -> throw IllegalArgumentException()
                }

                theirPlay to ourPlay
            }
            .map { (theirPlay, ourPlay) ->
                val outcome = ourPlay.battle(theirPlay)
                val shapeScore = when (ourPlay) {
                    Sign.Rock -> 1
                    Sign.Paper -> 2
                    Sign.Scissors -> 3
                }
                val outcomeScore = when (outcome) {
                    Result.Loss -> 0
                    Result.Draw -> 3
                    Result.Win -> 6
                }
                shapeScore + outcomeScore
            }
            .sum()
            .let(::println)
    }

    // Part 2
    aocReader(22, 2).useLines { lines ->
        lines
            .map {
                assert(it.length == 3)
                val theirPlay = when (it[0]) {
                    'A' -> Sign.Rock
                    'B' -> Sign.Paper
                    'C' -> Sign.Scissors
                    else -> throw IllegalArgumentException()
                }

                val outcome = when (it[2]) {
                    'X' -> Result.Loss
                    'Y' -> Result.Draw
                    'Z' -> Result.Win
                    else -> throw IllegalArgumentException()
                }

                theirPlay to outcome
            }
            .map { (theirPlay, outcome) ->
                val ourPlay = when (outcome) {
                    Result.Draw -> theirPlay
                    Result.Win -> when (theirPlay) {
                        Sign.Rock -> Sign.Paper
                        Sign.Paper -> Sign.Scissors
                        Sign.Scissors -> Sign.Rock
                    }

                    Result.Loss -> when (theirPlay) {
                        Sign.Rock -> Sign.Scissors
                        Sign.Paper -> Sign.Rock
                        Sign.Scissors -> Sign.Paper
                    }
                }

                val shapeScore = when (ourPlay) {
                    Sign.Rock -> 1
                    Sign.Paper -> 2
                    Sign.Scissors -> 3
                }
                val outcomeScore = when (outcome) {
                    Result.Loss -> 0
                    Result.Draw -> 3
                    Result.Win -> 6
                }
                shapeScore + outcomeScore
            }
            .sum()
            .let(::println)
    }
}
