package uk.co.mjdk.aoc24.day14

import uk.co.mjdk.aoc.aoc

fun Long.posMod(other: Long): Long {
    val res = this % other
    return if (res < 0L) res + other else res
}

data class Vector(val x: Long, val y: Long) {
    operator fun plus(v: Vector) = Vector(x + v.x, y + v.y)
    operator fun minus(v: Vector) = Vector(x - v.x, y - v.y)
    operator fun times(scalar: Long) = Vector(x * scalar, y * scalar)
    fun constrain(v: Vector) = Vector(x.posMod(v.x), y.posMod(v.y))
}

data class Robot(val initialPos: Vector, val velocity: Vector) {
    fun positionAfter(seconds: Long): Vector = initialPos + velocity * seconds

    companion object {
        private val pat = Regex("""p=(-?\d+),(-?\d+) v=(-?\d+),(-?\d+)""")
        fun parse(input: String): Robot {
            return pat.matchEntire(input)?.destructured?.let { (px, py, vx, vy) ->
                Robot(Vector(px.toLong(), py.toLong()), Vector(vx.toLong(), vy.toLong()))
            } ?: throw IllegalArgumentException("Invalid input $input")
        }
    }
}

enum class VerticalHalf {
    Left, Right
}

enum class HorizontalHalf {
    Top, Bottom
}

data class Quadrant(val v: VerticalHalf, val h: HorizontalHalf)

fun main() = aoc(2024, 14, { it.lines().map(Robot::parse) }) {
    example(
        """
        p=0,4 v=3,-3
        p=6,3 v=-1,-3
        p=10,3 v=-1,2
        p=2,0 v=2,-1
        p=0,0 v=1,3
        p=3,0 v=-2,-2
        p=7,6 v=-1,-3
        p=3,0 v=-1,-2
        p=9,3 v=2,3
        p=7,3 v=-1,2
        p=2,4 v=2,-3
        p=9,5 v=-3,-3
    """.trimIndent()
    )

    part1 { robots ->
//        val extents = Vector(11L, 7L)
        val extents = Vector(101L, 103L)
        val positions = robots.map { robot ->
            robot.positionAfter(100).constrain(extents)
        }
        val middle = Vector((extents.x / 2), (extents.y / 2))
        positions.mapNotNull { pos ->
            val v = when {
                pos.x < middle.x -> VerticalHalf.Left
                pos.x > middle.x -> VerticalHalf.Right
                else -> null
            }
            val h = when {
                pos.y < middle.y -> HorizontalHalf.Top
                pos.y > middle.y -> HorizontalHalf.Bottom
                else -> null
            }
            if (h != null && v != null) Quadrant(v, h) else null
        }.groupingBy { it }.eachCount().values.reduce(Int::times)
    }
}
