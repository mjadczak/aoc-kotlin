package uk.co.mjdk.aoc23.day08

import uk.co.mjdk.aoc.aoc
import uk.co.mjdk.aoc.repeatForever

private data class Node(val id: String, val left: String, val right: String) {
    operator fun get(direction: Direction): String = when (direction) {
        Direction.L -> left
        Direction.R -> right
    }

    companion object {
        fun fromLine(line: String): Node {
            // should just import a PEG library for these
            // 0123456789012345
            // BRR = (LVC, FSJ)
            require(line.length == 16)
            return Node(line.slice(0..2), line.slice(7..9), line.slice(12..14))
        }
    }
}

private enum class Direction {
    R, L;

    companion object {
        fun valueOf(char: Char): Direction = when (char) {
            'R' -> R
            'L' -> L
            else -> throw IllegalArgumentException(char.toString())
        }
    }
}

fun main() = aoc(2023, 8) {
    part1 { input ->
        val seq = input.lineSequence()
        val directions = seq.first().map(Direction::valueOf)
        val nodes = seq.drop(2).map(Node::fromLine).associateBy { it.id }

        directions.repeatForever().runningFold("AAA") { node, direction ->
            nodes[node]!![direction]
        }.withIndex().find { it.value == "ZZZ" }!!.index
    }
}
