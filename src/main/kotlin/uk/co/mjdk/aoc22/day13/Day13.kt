package uk.co.mjdk.aoc22.day13

import uk.co.mjdk.aoc.aocInput

sealed interface Node : Comparable<Node> {
    data class Leaf(val value: Int) : Node {
        override fun compareTo(other: Node): Int {
            return if (other is Leaf) {
                value.compareTo(other.value)
            } else {
                Seq(listOf(this)).compareTo(other)
            }
        }
    }

    data class Seq(val children: List<Node>) : Node {
        override fun compareTo(other: Node): Int {
            if (other is Leaf) {
                return -other.compareTo(this)
            } else if (other is Seq) {
                val i1 = this.children.iterator()
                val i2 = other.children.iterator()

                while (i1.hasNext() && i2.hasNext()) {
                    val cmp = i1.next().compareTo(i2.next())
                    if (cmp != 0) return cmp
                }

                return if (!i1.hasNext() && !i2.hasNext()) {
                    0
                } else if (i1.hasNext()) {
                    1
                } else {
                    -1
                }
            }

            throw IllegalArgumentException()
        }
    }

    companion object {
        private class Parser(val input: String) {
            // omit bounds checks as we assume well-formed input
            private var i = 0

            fun parseSeq(): Seq {
                assert(input[i] == '[')
                i++
                val children = buildList {
                    while (input[i] != ']') {
                        add(parseNode())
                        if (input[i] == ',') {
                            i++
                        }
                    }
                    i++
                }
                return Seq(children)
            }

            fun parseLeaf(): Leaf {
                var v = 0
                while (input[i].isDigit()) {
                    v *= 10
                    v += input[i].digitToInt()
                    i++
                }
                return Leaf(v)
            }

            fun parseNode(): Node {
                return if (input[i] == '[') {
                    parseSeq()
                } else {
                    parseLeaf()
                }
            }
        }

        fun parse(input: String): Node = Parser(input).parseNode()
    }


}

fun main() {
    aocInput(22, 13).useLines { lines ->
        lines
            .filterNot { it.isBlank() }
            .map(Node::parse)
            .chunked(2) { (a, b) ->
                a to b
            }
            .withIndex()
            .filter { iv -> iv.value.first < iv.value.second }
            .sumOf { it.index + 1 }
            .let(::println)
    }
}
