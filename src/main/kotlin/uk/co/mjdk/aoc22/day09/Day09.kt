package uk.co.mjdk.aoc22.day09

import uk.co.mjdk.aoc.aocInput
import kotlin.math.abs

data class Pos(val x: Int, val y: Int) {
    fun moved(dir: Dir): Pos =
        when (dir) {
            Dir.Right -> copy(x = x + 1)
            Dir.Down -> copy(y = y - 1)
            Dir.Left -> copy(x = x - 1)
            Dir.Up -> copy(y = y + 1)
        }
}

fun newTailPos(tail: Pos, head: Pos): Pos {
    val xDist = head.x - tail.x
    val yDist = head.y - tail.y
    val absXDist = abs(xDist)
    val absYDist = abs(yDist)

    if (absXDist < 2 && absYDist < 2) {
        return tail
    } else if (absXDist > absYDist) {
        assert(xDist != 0)
        return if (xDist > 0) {
            // Head is to the right
            head.copy(x = head.x - 1)
        } else {
            // Head is to the left
            head.copy(x = head.x + 1)
        }
    } else if (absYDist > absXDist) {
        assert(yDist != 0)
        return if (yDist > 0) {
            // Head is above
            head.copy(y = head.y - 1)
        } else {
            // Head is below
            head.copy(y = head.y + 1)
        }
    } else {
        throw IllegalStateException("Should not end up exactly diagonal with distance > 1, but had xDist=$xDist, yDist=$yDist")
    }
}

enum class Dir {
    Right,
    Down,
    Left,
    Up;

    companion object {
        fun parse(s: String): Dir =
            when (s) {
                "R" -> Right
                "D" -> Down
                "L" -> Left
                "U" -> Up
                else -> throw IllegalArgumentException("$s not valid")
            }

    }
}

fun main() {
    var head = Pos(0, 0)
    var tail = Pos(0, 0)
    val visited = mutableSetOf(tail)

    aocInput(22, 9).useLines { lines ->
        lines.forEach { line ->
            val (sDir, sNum) = line.split(' ')
            val num = sNum.toInt()
            val dir = Dir.parse(sDir)
            repeat(num) {
                head = head.moved(dir)
                tail = newTailPos(tail, head)
                visited += tail
            }
        }
        print(visited.count())
    }
}
