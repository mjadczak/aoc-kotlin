package uk.co.mjdk.aoc23

import uk.co.mjdk.aoc.aoc
import java.util.NavigableMap
import java.util.TreeMap

private data class Region(val toStart: Long, val fromStart: Long, val length: Long) {
    init {
        require(length > 0)
    }

    val fromRange = fromStart..<fromStart + length

    fun mapOrNull(from: Long): Long? = if (from in fromRange) toStart + (from - fromStart) else null
}

private class Mapper(regions: Iterable<Region>) {
    private val map: NavigableMap<Long, Region> = TreeMap()

    init {
        regions.forEach { r ->
            map[r.fromStart] = r
        }
    }

    operator fun invoke(from: Long): Long = map.floorEntry(from)?.value?.mapOrNull(from) ?: from
}

fun main() = aoc(2023, 5) {
    part1 { input ->
        val mappers = mutableListOf<Mapper>()
        val chunks = input.split("\n\n")
        val seeds = chunks.first().drop("seeds: ".length).trim().split(Regex("""\s+""")).map { it.toLong() }
        var lastCategory = "seed"
        chunks.drop(1).forEach { chunk ->
            val lines = chunk.trim().lines()
            val (from, _, to) = lines.first().split(" ").first().split("-")
            check(from == lastCategory) { "Expected mapping from $lastCategory but found from $from" }
            lastCategory = to

            val regions = lines.drop(1).map { line ->
                val (toStart, fromStart, length) = line.trim().split(Regex("""\s+""")).map { it.toLong() }
                Region(toStart, fromStart, length)
            }
            mappers.add(Mapper(regions))
        }
        check(lastCategory == "location") { "Last category $lastCategory" }

        fun mapSeed(seed: Long): Long = mappers.fold(seed) { v, mapper ->
            mapper(v)
        }

        seeds.minOf { mapSeed(it) }
    }
}
