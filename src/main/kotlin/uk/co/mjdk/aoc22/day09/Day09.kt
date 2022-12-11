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
        // We're diagonal, so move to the correct corner
        val newX = if (xDist > 0) head.x - 1 else head.x + 1
        val newY = if (yDist > 0) head.y - 1 else head.y + 1

        return Pos(newX, newY)
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

fun part1() {
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
        println(visited.count())
    }
}

fun part2() {
    val numKnots = 10
    var knots = List(numKnots) { Pos(0, 0) }
    val visited = mutableSetOf(knots[9])

    aocInput(22, 9).useLines { lines ->
        lines.forEach { line ->
            val (sDir, sNum) = line.split(' ')
            val num = sNum.toInt()
            val dir = Dir.parse(sDir)
            repeat(num) {
                knots = buildList(numKnots) {
                    add(knots[0].moved(dir))

                    for (i in 1 until numKnots) {
                        add(newTailPos(knots[i], get(i - 1)))
                    }
                }
                assert(knots.size == numKnots)
                visited += knots.last()
            }
        }
        println(visited.count())
    }
}

fun main() {
    part1()
    part2()
}
