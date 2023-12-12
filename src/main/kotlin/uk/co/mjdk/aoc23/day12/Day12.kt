package uk.co.mjdk.aoc23.day12

import uk.co.mjdk.aoc.aoc

private sealed interface Condition
private sealed interface Known : Condition
private data object Unknown : Condition
private data object Operational : Known
private data object Damaged : Known

private data class Row(val springs: List<Condition>, val counts: List<Int>)

private fun parse(input: String): List<Row> = input.lines().map { line ->
    val (conds, nums) = line.split(" ")
    Row(conds.map {
        when (it) {
            '.' -> Operational
            '#' -> Damaged
            '?' -> Unknown
            else -> throw IllegalArgumentException(it.toString())
        }
    }, nums.split(",").map { it.toInt() })
}

private fun groupings(conditions: Iterable<Known>): List<Int> = sequence {
    var groupSize = 0
    for (cond in conditions) {
        if (cond == Damaged) {
            groupSize += 1
        } else {
            check(cond == Operational)
            if (groupSize > 0) {
                yield(groupSize)
                groupSize = 0
            }
        }
    }
    if (groupSize > 0) {
        yield(groupSize)
    }
}.toList()

// key to our memo table:
// - we are considering some overall index (we only end up storing the ones we have to make a choice at)
// - we have completed some of the desired groups
// - we maybe have some existing "in-flight" group with some number of members
private data class State(
    val consideredIdx: Int,
    val numCompletedGroups: Int,
    val inFlightGroup: Int,
)

private sealed interface NextResult
private data object Valid : NextResult
private data object Invalid : NextResult
private data class Pending(val state: State) : NextResult

private class ArrangementsComputer private constructor(private val row: Row) {

    private val State.current: Condition
        get() = row.springs[consideredIdx]

    private fun State.pretty(): String = buildString {
        row.springs.forEach {
            when (it) {
                Unknown -> append('?')
                Damaged -> append('#')
                Operational -> append('.')
            }
        }
        append("\n")
        repeat(" ", consideredIdx)
        append('^')
        append("\n")
        append(
            "(in-flight: $inFlightGroup; no completed: $numCompletedGroups; remaining: ${
                row.counts.drop(
                    numCompletedGroups
                )
            })"
        )
        append("\n")
    }

    private fun State.next(element: Known): NextResult {
        val nextIdx = consideredIdx + 1
        val isAtEnd = nextIdx == row.springs.size
        val totalGroup = if (element == Operational) inFlightGroup else inFlightGroup + 1
        val nextTargetGroup = row.counts.getOrNull(numCompletedGroups)
        if (totalGroup > (nextTargetGroup ?: 0)) {
            return Invalid
        }
        val isEndGroup = (element == Operational || isAtEnd) && totalGroup > 0
        val (nextCompletedGroups, nextInFlightGroup) = if (!isEndGroup) {
            numCompletedGroups to totalGroup
        } else {
            if (totalGroup == nextTargetGroup) {
                numCompletedGroups + 1 to 0
            } else {
                return Invalid
            }
        }

        if (isAtEnd) {
            check(nextInFlightGroup == 0)
            return if (nextCompletedGroups == row.counts.size) Valid else Invalid
        }

        return Pending(State(nextIdx, nextCompletedGroups, nextInFlightGroup))
    }

    // For each State we might be in, and the choice we make, store the number of ways we can obtain the remaining groups (including any in-flight ones)
    val memo = mutableMapOf<Pair<State, Known>, Int>()

    // We could probably tabulate from the end rather than memoize from the start, but meh

    private fun numArrangements(): Int {
        val initial = State(0, 0, 0)
        return numArrangements(initial)
    }

    private fun numArrangements(nextResult: NextResult): Int = when (nextResult) {
        Valid -> 1
        Invalid -> 0
        is Pending -> numArrangements(nextResult.state)
    }

    private fun numArrangements(state: State): Int {
        return when (val el = state.current) {
            is Known -> numArrangements(state.next(el))
            Unknown -> listOf(Operational, Damaged).sumOf { choice ->
                val key = state to choice
                memo[key] ?: numArrangements(state.next(choice)).also { memo[key] = it }
            }
        }
    }


    companion object {
        operator fun invoke(row: Row): Int = ArrangementsComputer(row).numArrangements()
    }
}

fun main() = aoc(2023, 12, ::parse) {
    part1 { rows ->
        rows.sumOf { row ->
            ArrangementsComputer(row)
        }
    }
}
