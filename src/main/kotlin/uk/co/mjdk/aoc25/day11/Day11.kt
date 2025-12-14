package uk.co.mjdk.aoc25.day11

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.*
import uk.co.mjdk.aoc.aoc

data class Node(val id: String, val outputs: List<String>)

class Graph(nodes: List<Node>) {
    val edges = nodes.associate { it.id to it.outputs }.withDefault { emptyList() }
    val vertices = nodes.asSequence().flatMap { it.outputs.asSequence() + it.id }.toSet()
}

fun main() = aoc(2025, 11, object : Grammar<Graph>() {
    val id by regexToken("[a-z]{3}").map { it.text }
    val colon by literalToken(":")
    val space by literalToken(" ")
    val nl by literalToken("\n")
    val node by id and -colon and oneOrMore(-space and id) and -nl map { (id, outputs) -> Node(id, outputs) }
    override val root: Parser<Graph> by oneOrMore(node) map { Graph(it) }
}, trimString = false) {
    part1 { graph ->
        fun numPaths(from: String): Int {
            if (from == "out") return 1
            return graph.edges[from]!!.sumOf { next ->
                numPaths(next)
            }
        }
        numPaths("you")
    }

    part2 { graph ->
        // no cycles! so all paths must be svr -> fft -> dac -> out
        // and further, can divide up the graph by reachability
        fun numPaths(current: String, to: String, visited: MutableSet<String>, knownPast: Set<String>): Long {
            if (current == to) return 1L
            return graph.edges.getValue(current).filterNot { it in knownPast }.sumOf { next ->
                visited.add(next)
                numPaths(next, to, visited, knownPast)
            }
        }

        data class PartResult(val from: String, val to: String, val numPaths: Long, val visitedBetween: Set<String>) {
            val summary: String get() = "$from -> $to: $numPaths paths, ${visitedBetween.size} nodes"
        }

        fun numPaths2(from: String, to: String, reachableFromTo: Set<String> = emptySet()): PartResult {
            val visited = mutableSetOf<String>()
            val numPaths = numPaths(from, to, visited, reachableFromTo)
            return PartResult(from, to, numPaths, visited)
        }

        val dacOut = numPaths2("dac", "out").also { println(it.summary) }
        val fftDac = numPaths2("fft", "dac", dacOut.visitedBetween).also { println(it.summary) }
        val svrFft = numPaths2("svr", "fft", dacOut.visitedBetween + fftDac.visitedBetween).also { println(it.summary) }
        dacOut.numPaths * fftDac.numPaths * svrFft.numPaths
    }
}
