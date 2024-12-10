package uk.co.mjdk.aoc24.day05

import uk.co.mjdk.aoc.aoc

data class Input(val rules: List<Pair<Int, Int>>, val updates: List<List<Int>>) {
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

fun main() = aoc(2024, 5, Input::parse) {
    part1 { input ->
        val rulesBySuccessor = input.rules.groupBy { it.second }
        input.updates.asSequence().filter { pages ->
            val disallowedPages = mutableSetOf<Int>()
            for (p in pages) {
                if (p in disallowedPages) return@filter false
                disallowedPages.addAll(rulesBySuccessor[p].orEmpty().map { it.first })
            }
            true
        }.sumOf { pages ->
            check(pages.size % 2 == 1)
            pages[pages.size / 2]
        }
    }
}
