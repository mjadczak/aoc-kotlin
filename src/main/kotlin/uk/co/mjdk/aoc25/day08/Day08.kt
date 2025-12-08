package uk.co.mjdk.aoc25.day08

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.*
import uk.co.mjdk.aoc.aoc
import java.util.PriorityQueue

data class Box(val x: Long, val y: Long, val z: Long) {
    infix fun dist2(other: Box): Long = (x - other.x).squared() + (y - other.y).squared() + (z - other.z).squared()

    companion object {
        private fun Long.squared() = this * this
    }
}

data class BoxPair(val a: Box, val b: Box) : Comparable<BoxPair> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BoxPair) return false
        return a == other.a && b == other.b || a == other.b && b == other.a
    }

    override fun compareTo(other: BoxPair): Int = dist2.compareTo(other.dist2)

    val dist2 by lazy {
        a dist2 b
    }

    override fun hashCode(): Int = a.hashCode() + b.hashCode()
}

data class Circuit(val boxes: Set<Box>) {
    val size: Int get() = boxes.size
}

class MutableCircuitSet(boxes: Set<Box>) {
    private val boxToCircuit = boxes.associateWithTo(mutableMapOf()) { Circuit(setOf(it)) }
    private val circuits = boxToCircuit.values.toMutableSet()

    fun connect(a: Box, b: Box): Boolean {
        val circA = boxToCircuit[a] ?: error("Invalid box $a")
        val circB = boxToCircuit[b] ?: error("Invalid box $b")
        if (circA === circB) return false
        // could do mutable if needed for performance
        val newCirc = Circuit(circA.boxes + circB.boxes)
        circuits.remove(circA)
        circuits.remove(circB)
        circuits.add(newCirc)
        newCirc.boxes.forEach { box -> boxToCircuit[box] = newCirc }
        return true
    }

    fun orderedCircuits(): List<Circuit> = circuits.sortedByDescending { it.boxes.size }
    val numCircuits: Int get() = circuits.size
}

fun main() = aoc(2025, 8, object : Grammar<List<Box>>() {
    val coord by regexToken("\\d+").map { it.text.toLong() }
    val sep by literalToken(",")
    val nl by literalToken("\n")
    val box by coord and -sep and coord and -sep and coord and -nl map { (x, y, z) -> Box(x, y, z) }

    override val root: Parser<List<Box>> by oneOrMore(box)
}, trimString = false) {
    example(
        """162,817,812
57,618,57
906,360,560
592,479,940
352,342,300
466,668,158
542,29,236
431,825,988
739,650,466
52,470,668
216,146,977
819,987,18
117,168,530
805,96,715
346,949,466
970,615,88
941,993,340
862,61,35
984,92,344
425,690,689
"""
    )

    part1 { boxes ->
        val pairs =
            boxes
                .asSequence()
                .flatMap { a -> boxes.asSequence().map { b -> BoxPair(a, b) } }
                .filterNot { bp -> bp.a == bp.b }
                .toSet()
        val pq = PriorityQueue(pairs)
        val circuits = MutableCircuitSet(boxes.toSet())
        repeat(1000) {
            // could remove pairs in the circuit from the queue here, if we wanted
            requireNotNull(pq.poll()) { "No more pairs?" }.let {
                circuits.connect(it.a, it.b)
            }
        }
        circuits.orderedCircuits()
            .take(3)
            .map { it.boxes.size.toLong() }
            .reduce { a, b -> a * b }
    }

    part2 { boxes ->
        val pairs =
            boxes
                .asSequence()
                .flatMap { a -> boxes.asSequence().map { b -> BoxPair(a, b) } }
                .filterNot { bp -> bp.a == bp.b }
                .toSet()
        val pq = PriorityQueue(pairs)
        val circuits = MutableCircuitSet(boxes.toSet())
        var lastPair: BoxPair? = null
        while (circuits.numCircuits > 1) {
            requireNotNull(pq.poll()) { "No more pairs?" }.let {
                circuits.connect(it.a, it.b)
                lastPair = it
            }
        }
        checkNotNull(lastPair)
        lastPair.a.x * lastPair.b.x
    }
}
