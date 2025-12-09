package uk.co.mjdk.aoc25.day09

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import uk.co.mjdk.aoc.aoc
import kotlin.math.abs

data class Coord(val x: Long, val y: Long)

data class Rect(val a: Coord, val b: Coord) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Rect) return false
        return (a == other.a && b == other.b) || (a == other.b && b == other.a)
    }

    override fun hashCode(): Int = a.hashCode() + b.hashCode()

    val area: Long get() = (abs(a.x - b.x) + 1L) * (abs(a.y - b.y) + 1L)
}

fun main() = aoc(2025, 9, object : Grammar<List<Coord>>() {
    val int by regexToken("\\d+").map { it.text.toLong() }
    val sep by literalToken(",")
    val nl by literalToken("\n")
    val coord by int and -sep and int and -nl map { (x, y) -> Coord(x, y) }
    override val root: Parser<List<Coord>> by oneOrMore(coord)
}, trimString = false) {
    part1 { coords ->
        val pairs = coords.asSequence().flatMap { a ->
            coords.asSequence().filterNot { it == a }.map { b -> Rect(a, b) }
        }.toSet()
        pairs.maxOf { it.area }
    }
}
