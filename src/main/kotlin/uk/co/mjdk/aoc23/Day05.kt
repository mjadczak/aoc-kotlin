package uk.co.mjdk.aoc23

import uk.co.mjdk.aoc.aoc
import java.util.NavigableMap
import java.util.TreeMap
import kotlin.math.max
import kotlin.math.min

private fun (LongRange).entirelyBefore(other: LongRange): Boolean = this.last < other.first
private fun (LongRange).entirelyAfter(other: LongRange): Boolean = this.first > other.last

// TODO: keep wishing I had a nice impl of set algebra for AoC problems!

private data class Region(val toStart: Long, val fromStart: Long, val length: Long) {
    init {
        require(length > 0)
    }

    val fromRange = fromStart..<fromStart + length
    val toRange = toStart..<toStart + length

    fun mapOrNull(from: Long): Long? = if (from in fromRange) toStart + (from - fromStart) else null

    // Returns the subset of the range before, the mapped range overlapping, and the subset of the range after
    fun mapRange(from: LongRange): Triple<LongRange?, LongRange?, LongRange?> {
        val before = from.first..min(fromRange.first - 1, from.first)
        val after = max(fromRange.last + 1, from.first)..from.last
        val mappedFrom = max(from.first, fromRange.first)..min(from.last, fromRange.last)
        val mappedOffset = from.first - fromRange.first
        val mappedLen = mappedFrom.last - mappedFrom.first
        val mappedTo = toStart + mappedOffset..toStart + mappedOffset + mappedLen
        return Triple(
            before.takeUnless { it.isEmpty() },
            mappedTo.takeUnless { mappedFrom.isEmpty() },
            after.takeUnless { it.isEmpty() }
        )
        // TODO a debug function I can turn on and off in the fixture
        //   (or should I just use logging :) )
        //.also { println("$this($fromRange => $toRange)\n\t$from => $it") }
    }
}

private class Mapper(regions: Iterable<Region>) {
    private val map: NavigableMap<Long, Region> = TreeMap()

    init {
        regions.forEach { r ->
            map[r.fromStart] = r
        }
        map.sequencedValues().windowed(2).forEach { (r1, r2) ->
            require(r1.fromRange.entirelyBefore(r2.fromRange)) { "$r1 not before $r2" }
        }
    }

    operator fun invoke(from: Long): Long = map.floorEntry(from)?.value?.mapOrNull(from) ?: from

    operator fun invoke(from: RangeSet): RangeSet {
        // cross our fingers that they've made this nice enough for the mappings not to overlap
        //   edit: they have not
        val ranges = mutableListOf<LongRange>()

        for (range in from.ranges) {
            var thisRange: LongRange? = range
            for (region in map.sequencedValues()) {
                if (thisRange == null) break
                val (before, mapped, after) = region.mapRange(thisRange)
                before?.let { ranges.add(it) }
                mapped?.let { ranges.add(it) }
                thisRange = after
            }
            thisRange?.let { ranges.add(it) }
        }

        return RangeSet(ranges)
    }

    override fun toString(): String = buildString {
        append("Mapper(")
        map.sequencedValues().joinTo(this, "; ") { region ->
            "${region.fromRange} => ${region.toRange}"
        }
        append(")")
    }
}

private class RangeSet(rangesIn: Iterable<LongRange>) {
    val ranges: List<LongRange> = coalesce(rangesIn)

    init {
        ranges.windowed(2).forEach { (r1, r2) ->
            require(r1.entirelyBefore(r2)) { "$r1 not before $r2" }
        }
    }

    fun first(): Long = ranges.first().first

    override fun toString(): String {
        return "RangeSet(ranges=$ranges)"
    }

    companion object {
        private fun coalesce(ranges: Iterable<LongRange>): List<LongRange> {
            // TODO may be a good usecase for sequences instead of adding to an out list
            val out = mutableListOf<LongRange>()
            var thisRange: LongRange? = null
            for (range in ranges.filterNot { it.isEmpty() }.sortedBy { it.first }) {
                if (thisRange == null) {
                    thisRange = range
                    continue
                }
                if (thisRange.last >= range.first) {
                    thisRange = min(thisRange.first, range.first)..max(thisRange.last, range.last)
                } else {
                    out.add(thisRange)
                    thisRange = range
                }
            }
            thisRange?.let(out::add)
            return out
        }
    }
}

private fun parse(input: String): Pair<List<Long>, List<Mapper>> {
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
    return seeds to mappers
}

fun main() = aoc(2023, 5, ::parse) {
    example(
        """
        seeds: 79 14 55 13

        seed-to-soil map:
        50 98 2
        52 50 48

        soil-to-fertilizer map:
        0 15 37
        37 52 2
        39 0 15

        fertilizer-to-water map:
        49 53 8
        0 11 42
        42 0 7
        57 7 4

        water-to-light map:
        88 18 7
        18 25 70

        light-to-temperature map:
        45 77 23
        81 45 19
        68 64 13

        temperature-to-humidity map:
        0 69 1
        1 0 69

        humidity-to-location map:
        60 56 37
        56 93 4
    """.trimIndent()
    )

    part1 { (seeds, mappers) ->
        fun mapSeed(seed: Long): Long = mappers.fold(seed) { v, mapper ->
            mapper(v)
        }

        seeds.minOf { mapSeed(it) }
    }

    part2 { (seeds, mappers) ->
        val seedRanges =
            seeds.windowed(2, 2, partialWindows = true) // partial windows true so we get an error on odd number
                .map { (start, len) -> start..<(start + len) }
                //.also(::println)
                .let(::RangeSet)
        mappers.fold(seedRanges) { r, mapper ->
            mapper(r)
            //.also { println("IN:\n\t$r\nMAP:\n\t$mapper\nOUT:\n\t$it\n") }
        }.first()
    }
}
