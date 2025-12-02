package uk.co.mjdk.aoc25.day01

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import uk.co.mjdk.aoc.aoc
import uk.co.mjdk.aoc.arithMod

enum class Direction {
    Left,
    Right,
}

data class Rotation(val direction: Direction, val amount: Int) {
    init {
        require(amount > 0)
    }

    val offset: Int
        get() = when (direction) {
            Direction.Left -> -amount
            Direction.Right -> amount
        }
}

val grammar = object : Grammar<List<Rotation>>() {
    val direction by regexToken("[LR]").map {
        when (it.text) {
            "L" -> Direction.Left
            "R" -> Direction.Right
            else -> error(it)
        }
    }
    val eol by literalToken("\n")

    val number by regexToken("\\d+").map { it.text.toInt() }

    val rotation by parser {
        Rotation(direction(), number())
    }

    val rotations by parser {
        repeatZeroOrMore(parser {
            rotation().also {
                optional(eol)()
            }
        })
    }

    override val root by rotations
}

fun main() = aoc(2025, 1, { grammar.parse(it).getOrElse { error(it) } }) {
    part1 { rotations ->
        rotations
            .asSequence()
            .scan(50) { pos, rot ->
                (pos + rot.offset) arithMod 100
            }
            .count { it == 0 }
    }

    part2 { rotations ->
        var count = 0
        var position = 50
        for (r in rotations) {
            val initial = position
            count += r.amount / 100
            val modOffset = r.offset % 100
            val extendedPos = position + modOffset
            position = (position + modOffset) arithMod 100
            val didClick = when {
                extendedPos <= 0 && initial > 0 -> true
                extendedPos >= 100 -> true
                else -> false
            }
            if (didClick) count += 1
        }
        count
    }
}
