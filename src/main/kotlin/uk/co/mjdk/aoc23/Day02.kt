package uk.co.mjdk.aoc23

import uk.co.mjdk.aoc.aoc

private data class Round(val red: Int, val green: Int, val blue: Int) {
    val isPossible: Boolean
        get() = red <= 12 && green <= 13 && blue <= 14

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

    companion object {
        fun parse(input: String): Game {
            val (gam, rounds) = input.split(": ")
            val id = gam.drop("Game ".length).toInt()
            return Game(id, rounds.split("; ").map { Round.parse(it) })
        }
    }
}

fun main() = aoc(2023, 2) {
    fun getGames(input: String): List<Game> = input.lines().map { Game.parse(it) }

    part1 { input -> getGames(input).filter { it.isPossible }.sumOf { it.id } }
}
