package uk.co.mjdk.aoc23.day24

import uk.co.mjdk.aoc.aoc

private data class Coord(val x: Long, val y: Long, val z: Long) {
    val xy: Vector2
        get() = Vector2(x.toDouble(), y.toDouble())

    override fun toString(): String = "($x, $y, $z)"
}

private data class Vector2(val x: Double, val y: Double) {
    fun dot(other: Vector2): Double = x * other.x + y * other.y
    fun perp(): Vector2 = Vector2(-y, x)
    operator fun unaryMinus(): Vector2 = Vector2(-x, -y)
    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
    operator fun times(other: Double): Vector2 = Vector2(x * other, y * other)
    fun isFinite(): Boolean = x.isFinite() && y.isFinite()

    override fun toString(): String = "($x, $y)"
}

private operator fun Double.times(other: Vector2) = other * this

private data class Hailstone(val position: Coord, val velocity: Coord) {
    companion object {
        fun parse(input: String): Hailstone {
            val (pos, vel) = input.split(" @ ")
            val (px, py, pz) = pos.split(", ").map { it.trim().toLong() }
            val (vx, vy, vz) = vel.split(", ").map { it.trim().toLong() }
            return Hailstone(Coord(px, py, pz), Coord(vx, vy, vz))
        }
    }
}


fun main() = aoc(2023, 24, { it.lines().map(Hailstone::parse) }) {
    example(
        """
        19, 13, 30 @ -2,  1, -2
        18, 19, 22 @ -1, -1, -2
        20, 25, 34 @ -2, -2, -4
        12, 31, 28 @ -1, -2, -1
        20, 19, 15 @  1, -5, -3
    """.trimIndent()
    )

    part1 { hailstones ->
        // a useful writeup is at https://www.av8n.com/physics/points-lines.htm

        // but here, direction actually matters!

        val pairs = hailstones.asSequence().flatMapIndexed { idx, fst ->
            hailstones.asSequence().drop(idx + 1).map { snd ->
                fst to snd
            }
        }

        val target = 200_000_000_000_000.0..400_000_000_000_000.0
//        val target = 7.0..27.0

        pairs.count { (h1, h2) ->
            val a = h1.position.xy
            val b = h2.position.xy
            val da = h1.velocity.xy
            val db = h2.velocity.xy
            val pa = da.perp()
            val pb = db.perp()

            // these are distances along the direction for each hailstone
            val alpha = (b - a).dot(pb) / pb.dot(da)
            val beta = (a - b).dot(pa) / pa.dot(db)

            if (!alpha.isFinite() || !beta.isFinite() || alpha < 0 || beta < 0) {
//                println("$a@$da x $b@$db -> $intersection [NO; α=$alpha, β=$beta]")
                false
            } else {
                val intersection = a + alpha * da
                val isInRange = intersection.x in target && intersection.y in target
//                println("$a@$da x $b@$db -> $intersection [${if (isInRange) "OK" else "NO"}]")
                isInRange
            }
        }
    }
}
