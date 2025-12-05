package uk.co.mjdk.aoc25.day05

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import uk.co.mjdk.aoc.aoc
import java.util.*

data class Input(
    val ranges: List<LongRange>,
    val ids: List<Long>,
)

class MultiRange(ranges: Iterable<LongRange>) {
    private val rangeMap: NavigableMap<Long, LongRange> =
        sequence {
            val sorted = ranges.sortedBy { it.first }.asSequence()
            var current = sorted.first()
            for (next in sorted.drop(1)) {
                check(next.first >= current.first)
                if (next.first <= current.last) {
                    current = current.first..maxOf(current.last, next.last)
                } else {
                    yield(current)
                    current = next
                }
            }
            yield(current)
        }.associateByTo(TreeMap()) { it.first }

    operator fun contains(id: Long): Boolean {
        val range = rangeMap.floorEntry(id)?.value ?: return false
        return id in range
    }

    val size: Long get() = rangeMap.values.sumOf { it.last - it.first + 1L }
}

fun main() = aoc(2025, 5, object : Grammar<Input>() {
    val long by regexToken("\\d+").map { it.text.toLong() }
    val dash by literalToken("-")
    val nl by literalToken("\n")
    val range by long and -dash and long map { (fst, snd) -> fst..snd }
    val ranges by oneOrMore(range and -optional(nl))
    val ids by oneOrMore(long and -optional(nl))
    override val root: Parser<Input> by ranges and -nl and ids map { (r, i) -> Input(r, i) }
}) {
    part1 { input ->
        val mr = MultiRange(input.ranges)
        input.ids.count { it in mr }
    }

    part2 { input ->
        val mr = MultiRange(input.ranges)
        mr.size
    }
}
