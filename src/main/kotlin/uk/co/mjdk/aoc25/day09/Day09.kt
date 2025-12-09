package uk.co.mjdk.aoc25.day09

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import uk.co.mjdk.aoc.aoc
import java.util.*
import kotlin.math.abs

data class Coord(val x: Int, val y: Int)

data class Rect(val a: Coord, val b: Coord) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rect) return false
        return (a == other.a && b == other.b) || (a == other.b && b == other.a)
    }

    override fun hashCode(): Int = a.hashCode() + b.hashCode()

    val area: Long get() = (abs(a.x - b.x) + 1L) * (abs(a.y - b.y) + 1L)

    val otherCorners: Sequence<Coord> = sequenceOf(
        Coord(a.x, b.y),
        Coord(b.x, a.y),
    )
}

class Board(coords: List<Coord>) {
    private val verticalEdges: NavigableMap<Int, List<VertEdge>>
    private val horizontalEdges: NavigableMap<Int, List<HorizEdge>>
    private val vertices = coords.toSet()

    init {
        require(coords.isNotEmpty())

        val vertical = mutableListOf<VertEdge>()
        val horizontal = mutableListOf<HorizEdge>()

        (coords.asSequence() + coords.first()).zipWithNext()
            .forEach { (from, to) ->
                if (from.x == to.x) {
                    val minY = minOf(from.y, to.y)
                    val maxY = maxOf(from.y, to.y)
                    vertical.add(VertEdge(from.x, minY..<maxY))
                } else {
                    check(from.y == to.y)
                    val minX = minOf(from.x, to.x)
                    val maxX = maxOf(from.x, to.x)
                    horizontal.add(HorizEdge(from.y, minX..<maxX))
                }
            }
        verticalEdges = TreeMap(vertical.groupBy { it.x })
        horizontalEdges = TreeMap(horizontal.groupBy { it.y })
    }

    private data class VertEdge(val x: Int, val ys: IntRange)
    private data class HorizEdge(val y: Int, val xs: IntRange)

    operator fun contains(coord: Coord): Boolean {
        // inspired by PNPOLY
        // shoot a ray to the right, and count the number of edges intersected
        // if point is _on_ the edge (including on vertex) then just handle explicitly
        verticalEdges[coord.x]?.forEach { edge ->
            if (coord.y in edge.ys) return true
        }

        horizontalEdges[coord.y]?.forEach { edge ->
            if (coord.x in edge.xs) return true
        }

        val intersections = verticalEdges.tailMap(coord.x).values.asSequence().flatten().count { edge ->
            coord.y in edge.ys
        }
        return intersections % 2 == 1
    }

    operator fun contains(rect: Rect): Boolean {
        check(rect.a in vertices && rect.b in vertices)
        if (rect.otherCorners.any { it !in this }) return false
        val ordered = (rect.otherCorners + rect.a + rect.b).sortedWith(compareBy({ it.x }, { it.y })).groupBy { it.x }
        check(ordered.size in 1..2) { ordered }
        val left = ordered.values.first()
        val right = ordered.values.last()
        val topLeft = left.first()
        val botLeft = left.last()
        val topRight = right.first()
        val botRight = right.last()

        val horizEdges =
            listOf(HorizEdge(topLeft.y, topLeft.x..topRight.x), HorizEdge(botLeft.y, botLeft.x..botRight.x))
        val vertEdges = listOf(VertEdge(topLeft.x, topLeft.y..botLeft.y), VertEdge(topRight.x, topRight.y..botRight.y))

        horizEdges.forEach { rectEdge ->
            val intersects =
                verticalEdges.subMap(rectEdge.xs.first, false, rectEdge.xs.last, false).values.asSequence().flatten()
                    .any { rectEdge.y in it.ys }
            if (intersects) return false
        }

        vertEdges.forEach { rectEdge ->
            val intersects =
                horizontalEdges.subMap(rectEdge.ys.first, false, rectEdge.ys.last, false).values.asSequence().flatten()
                    .any { rectEdge.x in it.xs }
            if (intersects) return false
        }

        return true
    }
}

fun main() = aoc(2025, 9, object : Grammar<List<Coord>>() {
    val int by regexToken("\\d+").map { it.text.toInt() }
    val sep by literalToken(",")
    val nl by literalToken("\n")
    val coord by int and -sep and int and -nl map { (x, y) -> Coord(x, y) }
    override val root: Parser<List<Coord>> by oneOrMore(coord)
}, trimString = false) {
    example(
        """7,1
11,1
11,7
9,7
9,5
2,5
2,3
7,3
"""
    )

    part1 { coords ->
        val pairs = coords.asSequence().flatMap { a ->
            coords.asSequence().filterNot { it == a }.map { b -> Rect(a, b) }
        }.toSet()
        pairs.maxOf { it.area }
    }

    part2 { coords ->
        val board = Board(coords)
        val pairs = coords.asSequence().flatMap { a ->
            coords.asSequence().filterNot { it == a }.map { b -> Rect(a, b) }
        }.toSet().filter {
            it in board
        }
        pairs.maxBy { it.area }.also { println(it) }.area
    }
}
