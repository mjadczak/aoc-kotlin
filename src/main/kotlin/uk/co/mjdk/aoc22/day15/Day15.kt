package uk.co.mjdk.aoc22.day15

import uk.co.mjdk.aoc.aocReader
import kotlin.math.abs

data class Pos(val x: Int, val y: Int) {
    fun manhattan(other: Pos): Int = abs(x - other.x) + abs(y - other.y)

    override fun toString(): String {
        return "(${x},${y})"
    }
}

data class Sensor(val pos: Pos, val nearestBeacon: Pos) {
    val manhattan: Int = pos.manhattan(nearestBeacon)

    fun covers(square: Pos): Boolean = square.manhattan(pos) <= manhattan

    fun corners(): List<Pos> = listOf(
        Pos(pos.x, pos.y - manhattan),
        Pos(pos.x, pos.y + manhattan),
        Pos(pos.x - manhattan, pos.y),
        Pos(pos.x + manhattan, pos.y)
    )

    fun expandedEdgeLines(): List<Line> = buildList {
        // top left
        add(Line(false, pos.x + pos.y - manhattan - 1))
        // bottom right
        add(Line(false, pos.x + pos.y + manhattan + 1))
        // top right
        add(Line(true, pos.y - manhattan - 1 - pos.x))
        // bottom left
        add(Line(true, pos.y + manhattan + 1 - pos.x))
    }
}

data class Line(val isPosGradient: Boolean, val yIntercept: Int) {
    fun intersection(other: Line): Pos? {
        if (isPosGradient == other.isPosGradient) {
            throw IllegalArgumentException("Need opposite gradient")
        }
        if (!isPosGradient) {
            return other.intersection(this)
        }
        // x + c1 = -x + c2
        // 2x = c2 - c1
        val c2c1 = other.yIntercept - yIntercept
        if (c2c1 % 2 != 0) {
            //throw IllegalArgumentException("Intersection not on integer coord")
            return null
        }
        val x = c2c1 / 2
        val y = x + yIntercept
        return Pos(x, y)
    }

    override fun toString(): String {
        return "y = ${if (isPosGradient) "" else "-"}x ${if (yIntercept < 0) "-" else "+"} ${abs(yIntercept)}"
    }
}

val inputPat = Regex("""Sensor at x=(-?\d+), y=(-?\d+): closest beacon is at x=(-?\d+), y=(-?\d+)""")

fun getSensors(): List<Sensor> = aocReader(22, 15).useLines { lines ->
    lines.map { line ->
        val (x1, y1, x2, y2) = inputPat.matchEntire(line)!!.groupValues.drop(1).map { it.toInt() }
        Sensor(Pos(x1, y1), Pos(x2, y2))
    }.toList()
}

fun part1() {
    val sensors = getSensors()
    val minX = sensors.minOfOrNull {
        it.pos.x - it.manhattan
    }!!
    val maxX = sensors.maxOfOrNull {
        it.pos.x + it.manhattan
    }!!
    val beacons = sensors.map { it.nearestBeacon }.toSet()
    val numPos = (minX..maxX).map { Pos(it, 2_000_000) }.filterNot { beacons.contains(it) }
        .count { pos -> sensors.any { it.covers(pos) } }
    println(numPos)
}

fun part2() {
    // For each sensor, we take the area of influence, then "expand" by one square, and take the four lines on which
    // the edges of this expanded area lie. The point we search for must lie in an intersection of such lines, or in a
    // corner. For simplicity, forget about line segments and just check all intersections of the lines (in the search
    // area).
    val sensors = getSensors()
    val limit = 4_000_000
    val (posLines, negLines) = sensors.flatMap { it.expandedEdgeLines() }.distinct().partition { it.isPosGradient }
    val corners = sequenceOf(Pos(0, 0), Pos(0, limit), Pos(limit, 0), Pos(limit, limit))
    val thePointL = (posLines.asSequence().flatMap { a -> negLines.asSequence().map { b -> a to b } }
        .map { (a, b) -> a.intersection(b) }.filterNotNull() + corners)
        .filter { it.x in 0..limit && it.y in 0..limit }
        .filterNot { pos -> sensors.any { it.covers(pos) } }
        .toList()
    assert(thePointL.size == 1)
    val thePoint = thePointL.first()
    println(thePoint.x.toLong() * 4_000_000L + thePoint.y)
}

fun main() {
    part1()
    part2()
}
