package uk.co.mjdk.aoc23

import uk.co.mjdk.aoc.aoc

// Top-left is 0,0
private data class Coord(val row: Int, val col: Int) {
    fun adjacent(): Sequence<Coord> = sequence {
        for (r in -1..1) {
            for (c in -1..1) {
                Coord(row + r, col + c).takeUnless { it == this@Coord }?.let { yield(it) }
            }
        }
    }
}

private sealed interface Item
private data class Symbol(val character: Char, val coord: Coord) : Item
private data class Number(val number: Int, val coords: Set<Coord>) : Item

private class Schematic private constructor(
    private val board: Map<Coord, Item>,
    val symbols: Set<Symbol>,
    val numbers: Set<Number>
) {
    operator fun get(coord: Coord): Item? = board[coord]

    companion object {
        fun parse(input: String): Schematic {
            val board = mutableMapOf<Coord, Item>()
            val symbols = mutableSetOf<Symbol>()
            val numbers = mutableSetOf<Number>()

            fun (Symbol).add() {
                board[coord] = this
                symbols.add(this)
            }

            fun (Number).add() {
                coords.forEach {
                    board[it] = this
                }
                numbers.add(this)
            }

            input.lineSequence().withIndex().forEach { (rowNo, rowStr) ->
                var num: Number? = null
                rowStr.withIndex().forEach col@{ (colNo, char) ->
                    val coord = Coord(rowNo, colNo)
                    if (char.isDigit()) {
                        num = num.let {
                            it?.copy(number = it.number * 10 + char.digitToInt(), coords = it.coords + coord)
                                ?: Number(char.digitToInt(), setOf(coord))
                        }
                        return@col
                    }

                    num?.add()
                    num = null

                    if (char != '.') {
                        Symbol(char, coord).add()
                    }
                }
                num?.add()
            }

            return Schematic(board, symbols, numbers)
        }
    }
}

fun main() = aoc(2023, 3, Schematic::parse) {
    part1 { board ->
        board.numbers.filter { n ->
            n.coords.asSequence().flatMap { it.adjacent() }.any { board[it] is Symbol }
        }.sumOf { it.number }
    }

    part2 { board ->
        board.symbols.filter { it.character == '*' }.mapNotNull { sym ->
            sym.coord.adjacent().mapNotNull { coord -> (board[coord] as? Number)?.number }.toSet() // toSet because otherwise we double-count the numbers
                .takeIf { it.size == 2 }?.reduce(Int::times)
        }.sum()
    }
}
