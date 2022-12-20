package uk.co.mjdk.aoc22.day15

import uk.co.mjdk.aoc.aocInput
import kotlin.math.abs

data class Pos(val x: Int, val y: Int) {
    fun manhattan(other: Pos): Int = abs(x - other.x) + abs(y - other.y)
}

data class Sensor(val pos: Pos, val nearestBeacon: Pos) {
    val manhattan: Int = pos.manhattan(nearestBeacon)
}

val inputPat = Regex("""Sensor at x=(-?\d+), y=(-?\d+): closest beacon is at x=(-?\d+), y=(-?\d+)""")

fun getSensors(): List<Sensor> = aocInput(22, 15).useLines { lines ->
    lines.map { line ->
        val (x1, y1, x2, y2) = inputPat.matchEntire(line)!!.groupValues.drop(1).map { it.toInt() }
        Sensor(Pos(x1, y1), Pos(x2, y2))
    }.toList()
}

fun main() {
    val sensors = getSensors()
    val minX = sensors.minOfOrNull {
        it.pos.x - it.manhattan
    }!!
    val maxX = sensors.maxOfOrNull {
        it.pos.x + it.manhattan
    }!!
    val beacons = sensors.map { it.nearestBeacon }.toSet()
    // Probably going to be unsustainable for part 2, but hey ho
    val numPos = (minX..maxX).map { Pos(it, 2_000_000) }.filterNot { beacons.contains(it) }
        .count { pos -> sensors.any { pos.manhattan(it.pos) <= it.manhattan } }
    println(numPos)
}
