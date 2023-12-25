package uk.co.mjdk.aoc23.day25

import uk.co.mjdk.aoc.aoc
import java.util.ArrayDeque

private class Graph {
    private val edges = mutableSetOf<String>()

    private fun key(from: String, to: String): String = if (from <= to) from + to else to + from

    fun connected(from: String, to: String): Boolean = key(from, to) in edges

    fun add(from: String, to: String) {
        require(from.length == 3 && to.length == 3)
        edges += key(from, to)
    }

    fun remove(from: String, to: String) {
        edges -= key(from, to)
    }

    fun all(): Sequence<Pair<String, String>> = edges.asSequence().map { it.take(3) to it.drop(3) }

    fun connections(): Map<String, Set<String>> {
        val map = mutableMapOf<String, MutableSet<String>>()
        fun edge(from: String, to: String) {
            map.computeIfAbsent(from) { mutableSetOf() }.add(to)
            map.computeIfAbsent(to) { mutableSetOf() }.add(from)
        }
        edges.forEach { s ->
            edge(s.take(3), s.drop(3))
        }
        return map
    }
}

private fun parse(input: String): Graph {
    val g = Graph()
    input.lineSequence().forEach { line ->
        val (from, tos) = line.split(": ")
        tos.split(" ").forEach {
            g.add(from, it)
        }
    }
    return g
}

fun main() = aoc(2023, 25, ::parse) {
    part1 { graph ->
        fun graphviz() {
            println("strict graph G {")
            graph.all().forEach { (l, r) ->
                println("  $l -- $r")
            }
            println("}")
        }

        // by inspection from neato - don't love it, but want to get on for now

        val edgesToCut = listOf(
            "jzj" to "vkb",
            "vrx" to "hhx",
            "grh" to "nvh",
        )


        edgesToCut.forEach { graph.remove(it.first, it.second) }

        // count components

        val conn = graph.connections()

        fun visit(start: String): Set<String> {
            val visited = mutableSetOf(start)
            val queue = ArrayDeque<String>()
            queue.offer(start)
            while (queue.isNotEmpty()) {
                val node = queue.poll()
                for (c in conn[node]!!) {
                    if (c !in visited) {
                        queue.offer(c)
                        visited += c
                    }
                }
            }
            return visited
        }

        val a = visit(edgesToCut[0].first)
        val b = visit(edgesToCut[0].second)
        a.size * b.size
    }
}
