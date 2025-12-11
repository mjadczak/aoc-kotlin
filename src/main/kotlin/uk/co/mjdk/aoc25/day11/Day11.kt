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
}
