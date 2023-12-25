package uk.co.mjdk.aoc23.day24

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.MatrixUtils
import uk.co.mjdk.aoc.aoc
import kotlin.math.sqrt


private data class Coord(val x: Long, val y: Long, val z: Long) {
    val xy: Vector2
        get() = Vector2(x.toDouble(), y.toDouble())

    val vector: Vector3
        get() = Vector3(x.toDouble(), y.toDouble(), z.toDouble())

    override fun toString(): String = "($x, $y, $z)"
}

private data class Vector2(val x: Double, val y: Double) {
    infix fun dot(other: Vector2): Double = x * other.x + y * other.y
    fun perp(): Vector2 = Vector2(-y, x)
    operator fun unaryMinus(): Vector2 = Vector2(-x, -y)
    operator fun plus(other: Vector2): Vector2 = Vector2(x + other.x, y + other.y)
    operator fun minus(other: Vector2): Vector2 = Vector2(x - other.x, y - other.y)
    operator fun times(other: Double): Vector2 = Vector2(x * other, y * other)

    override fun toString(): String = "($x, $y)"
}

private data class Vector3(val x: Double, val y: Double, val z: Double) {
    infix fun dot(other: Vector3): Double = x * other.x + y * other.y + z * other.z
    infix fun x(b: Vector3): Vector3 {
        val a = this
        return Vector3(
            a.y * b.z - a.z * b.y,
            a.z * b.x - a.x * b.z,
            a.x * b.y - a.y * b.x
        )
    }

    fun norm(): Double = sqrt(x * x + y * y + z * z)
    fun unit(): Vector3 = this / norm()

    operator fun unaryMinus(): Vector3 = Vector3(-x, -y, -z)
    operator fun plus(other: Vector3): Vector3 = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3): Vector3 = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(other: Double): Vector3 = Vector3(x * other, y * other, z * other)
    operator fun div(other: Double): Vector3 = Vector3(x / other, y / other, z / other)

    fun commons(): ArrayRealVector = ArrayRealVector(doubleArrayOf(x, y, z))

    override fun toString(): String = "($x, $y, $z)"

    companion object {
        val zero: Vector3 = Vector3(0.0, 0.0, 0.0)
    }
}

private operator fun Double.times(other: Vector2) = other * this
private operator fun Double.times(other: Vector3) = other * this

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

private fun <T> List<T>.allPairs(): Sequence<Pair<T, T>> = asSequence().flatMapIndexed { idx, fst ->
    asSequence().drop(idx + 1).map { snd ->
        fst to snd
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

        val target = 200_000_000_000_000.0..400_000_000_000_000.0

        hailstones.allPairs().count { (h1, h2) ->
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
                false
            } else {
                val intersection = a + alpha * da
                val isInRange = intersection.x in target && intersection.y in target
                isInRange
            }
        }
    }

    part2 { hailstones ->
        // thanks to https://old.reddit.com/r/adventofcode/comments/18pnycy/2023_day_24_solutions/kepu26z/ for
        // wrapping everything together

        // for each hailstone i, the relative initial position of the rock to the initial position of    the hailstone (p_r - p_i)
        // must be parallel to the relative velocity of the rock vs the hailstone (for all time!) (v_r - v_i), and therefore
        // (p_r - p_i) x (v_r - v_i) = 0
        // Now, expanding for a few i we find that the only non-linear term (p_r x v_r) is common, and thus by equating the above for i = (0, 1) and (1, 2)
        // we get a 6-unknowns 6-equations _linear_ system we can solve!
        // I tried doing this manually with a hand-rolled gaussian elimination based on the below,
        // but I could not get it to work - so let's give in and use an external linalg lib

        // Ax = b
        // A = |            0, (v0_z - v1_z), (v1_y - v0_y),             0, (p0_z - p1_z), (p1_y - p0_y)| x = |pr_x| b = |v1_y.p1_z - v1_z.p1_y - v0_y.p0_z + v0_z.p0_y|
        //     |(v1_z - v0_z),             0, (v0_x - v1_x), (p0_z - p1_z),             0, (p1_x - p0_x)|     |pr_y|     |v1_z.p1_x - v1_x.p1_z - v0_z.p0_x + v0_x.p0_z|
        //     |(v0_y - v1_y), (v1_x - v0_x),             0, (p0_y - p1_y), (p1_x - p0_x),             0|     |pr_z|     |v1_x.p1_y - v1_y.p1_x - v0_x.p0_y + v0_y.p0_x|
        //     |            0, (v1_z - v2_z), (v2_y - v1_y),             0, (p1_z - p2_z), (p2_y - p1_y)|     |pr_x|     |v2_y.p2_z - v2_z.p2_y - v1_y.p1_z + v1_z.p1_y|
        //     |(v2_z - v1_z),             0, (v1_x - v2_x), (p1_z - p2_z),             0, (p2_x - p1_x)|     |pr_y|     |v2_z.p2_x - v2_x.p2_z - v1_z.p1_x + v1_x.p1_z|
        //     |(v1_y - v2_y), (v2_x - v1_x),             0, (p1_y - p2_y), (p2_x - p1_x),             0|     |pr_z|     |v2_x.p2_y - v2_y.p2_x - v1_x.p1_y + v1_y.p1_x|

        fun Vector3.crossMatrix() = Array2DRowRealMatrix(
            arrayOf(
                doubleArrayOf(
                    0.0, -z, y,
                ),
                doubleArrayOf(
                    z, 0.0, -x,
                ),
                doubleArrayOf(
                    -y, x, 0.0
                ),
            ), false
        )


        // kmath probably has a nicer interface for some of this stuff but it seems half-baked
        operator fun <T> List<T>.component6(): T = get(5)
        val (p0, v0, p1, v1, p2, v2) = hailstones.take(3)
            .flatMap { listOf(it.position.vector, it.velocity.vector) }

        val M = Array2DRowRealMatrix(6, 6)
        val rhs = ArrayRealVector(6)

        rhs.setSubVector(0, ((p1 x v1) - (p0 x v0)).commons())
        rhs.setSubVector(3, ((p2 x v2) - (p0 x v0)).commons())

        M.setSubMatrix(v0.crossMatrix().subtract(v1.crossMatrix()).data, 0, 0)
        M.setSubMatrix(v0.crossMatrix().subtract(v2.crossMatrix()).data, 3, 0)
        M.setSubMatrix(p1.crossMatrix().subtract(p0.crossMatrix()).data, 0, 3)
        M.setSubMatrix(p2.crossMatrix().subtract(p0.crossMatrix()).data, 3, 3)

        val inv = MatrixUtils.inverse(M)
        val res = inv.operate(rhs)

        println("pos: ${res.getSubVector(0, 3)}")
        println("vel: ${res.getSubVector(3, 3)}")

        res.getSubVector(0, 3).toArray().sumOf { it.toLong() }


        // This solution has a bug. I ended up doing some manual tweakery to get the answer and might revisit this later
        // I think the bug is just in the precision of the operations, as I end up with an answer which is 3 off the correct one.
    }
}
