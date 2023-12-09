package uk.co.mjdk.aoc23.day02

import uk.co.mjdk.aoc.aoc

private data class Round(val red: Int, val green: Int, val blue: Int) {
    val isPossible: Boolean
        get() = red <= 12 && green <= 13 && blue <= 14

    val power: Int
        get() = red * green * blue

    companion object {
        fun parse(input: String): Round {
            val colours = input.split(", ").map { it.split(' ') }.associate { (num, colour) ->
                colour to num.toInt()
            }
            return Round(colours["red"] ?: 0, colours["green"] ?: 0, colours["blue"] ?: 0)
        }
    }
}

private data class Game(val id: Int, val rounds: List<Round>) {
    val isPossible: Boolean
        get() = rounds.all { it.isPossible }

    val minRound: Round
        get() = Round(rounds.maxOf { it.red }, rounds.maxOf { it.green }, rounds.maxOf { it.blue })

    companion object {
        fun parse(input: String): Game {
            val (gam, rounds) = input.split(": ")
            val id = gam.drop("Game ".length).toInt()
            return Game(id, rounds.split("; ").map { Round.parse(it) })
        }
    }
}

private fun getGames(input: String): List<Game> = input.lines().map { Game.parse(it) }

fun main() = aoc(2023, 2, ::getGames) {
    part1 { games -> games.filter { it.isPossible }.sumOf { it.id } }

    part2 { games ->
        games.sumOf { it.minRound.power }
    }
}
