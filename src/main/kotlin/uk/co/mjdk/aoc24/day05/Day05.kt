package uk.co.mjdk.aoc24.day05

import uk.co.mjdk.aoc.aoc
import kotlin.collections.groupBy
import kotlin.collections.map
import kotlin.collections.orEmpty

data class Input(val rules: List<Pair<Int, Int>>, val updates: List<List<Int>>) {
    val rulesBySuccessor = rules.groupBy { it.second }

    fun isValid(pages: List<Int>): Boolean {
        val disallowedPages = mutableSetOf<Int>()
        for (p in pages) {
            if (p in disallowedPages) return false
            disallowedPages.addAll(rulesBySuccessor[p].orEmpty().map { it.first })
        }
        return true
    }

    fun topoSort(pages: List<Int>): List<Int> {
        val pageSet = pages.toSet()
        val relevantRules = rules.filter { it.first in pageSet && it.second in pageSet }
        val incoming = relevantRules.groupByTo(LinkedHashMap<Int, MutableList<Int>>(), { it.second }, { it.first })
        val outgoing = relevantRules.groupBy({ it.first }, { it.second })
        val available = pageSet.filterNot { relevantRules.any { r -> r.second == it } }.toMutableSet()
        return buildList {
            while (available.isNotEmpty()) {
                val n = available.first()
                available.remove(n)
                add(n)

                outgoing[n]?.forEach { m ->
                    incoming[m]?.let { inList ->
                        check(inList.remove(n))
                        if (inList.isEmpty()) {
                            incoming.remove(m)
                            available.add(m)
                        }
                    }
                }

            }
        }.also { check(incoming.isEmpty()) }
    }

    companion object {
        fun parse(input: String): Input {
            val (rulesChunk, updatesChunk) = input.split("\n\n")
            val rules = rulesChunk.lines().map { line ->
                val (before, after) = line.split("|")
                before.toInt() to after.toInt()
            }
            val updates = updatesChunk.lines().map { line -> line.split(",").map { it.toInt() } }
            return Input(rules, updates)
        }
    }
}

fun List<Int>.middle(): Int {
    check(size % 2 == 1)
    return get(size / 2)
}

fun main() = aoc(2024, 5, Input::parse) {
    part1 { input ->
        input.updates.asSequence().filter(input::isValid).sumOf { it.middle() }
    }

    part2 { input ->
        input.updates.asSequence().filterNot(input::isValid).map(input::topoSort).sumOf { it.middle() }
    }
}
