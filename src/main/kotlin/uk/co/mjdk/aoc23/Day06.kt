package uk.co.mjdk.aoc23

import uk.co.mjdk.aoc.aoc
import kotlin.math.pow
import kotlin.math.min
import kotlin.math.max
import kotlin.math.floor
import kotlin.math.ceil
import kotlin.math.sqrt

private fun quadratic(a: Double, b: Double, c: Double): Pair<Double, Double> {
    val disc = sqrt(b.pow(2).minus(4 * a * c))
    val x1 = (-b - disc) / (2 * a)
    val x2 = (-b + disc) / (2 * a)
    return Pair(min(x1, x2), max(x1, x2))
}

private data class Race(val time: Long, val record: Long)

private fun parseRaces(input: String): List<Race> {
    // TODO just write a "parse whitespace-separated list of ints" function
    val (t, r) = input.lines()
    val ts = t.drop("Time:".length).trim().split(Regex("""\s+""")).map { it.toLong() }
    val rs = r.drop("Distance:".length).trim().split(Regex("""\s+""")).map { it.toLong() }
    return ts.zip(rs).map { (time, record) ->
        Race(time, record)
    }
}

private fun parseOneRace(input: String): Race {
    val (t, r) = input.lines()
    val time = t.drop("Time:".length).trim().split(Regex("""\s+""")).joinToString("").toLong()
    val record = r.drop("Distance:".length).trim().split(Regex("""\s+""")).joinToString("").toLong()
    return Race(time, record)
}

private fun (Race).getNumBetterThanRecord(): Long {
    // the function of distance d vs time to hold button t, where T is the max time, is
    // d = t(T-t) = -t^2 + Tt
    // This is always an inverted quadratic
    // To find the number of integer times t where d > D, we find the two spots t1, t2 where the parabola = D,
    // and find the number of the integers between t1 and t2 (not including t1 and t2)

    val (t1, t2) = quadratic(-1.0, time.toDouble(), -record.toDouble())
    return ceil(t2 - 1).toLong() - floor(t1 + 1).toLong() + 1
}

fun main() = aoc(2023, 6) {
    example(
        """
        Time:      7  15   30
        Distance:  9  40  200
    """.trimIndent()
    )

    part1 { input ->
        parseRaces(input).map { it.getNumBetterThanRecord() }.reduce(Long::times)
    }

    part2 { input ->
        parseOneRace(input).getNumBetterThanRecord()
    }
}
