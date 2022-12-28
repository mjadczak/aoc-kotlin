package uk.co.mjdk.aoc22.day17

import uk.co.mjdk.aoc.aocInput
import uk.co.mjdk.aoc.repeatForever
import kotlin.math.max

sealed interface Shape {
    // bottom-to-top, left-to-right, 4x4, aligned to bottom-left
    val cells: Array<BooleanArray>

    object Horiz : Shape {
        override val cells = arrayOf(
            booleanArrayOf(true, true, true, true),
            booleanArrayOf(false, false, false, false),
            booleanArrayOf(false, false, false, false),
            booleanArrayOf(false, false, false, false),
        )
    }

    object Plus : Shape {
        override val cells = arrayOf(
            booleanArrayOf(false, true, false, false),
            booleanArrayOf(true, true, true, false),
            booleanArrayOf(false, true, false, false),
            booleanArrayOf(false, false, false, false),
        )
    }

    object Angle : Shape {
        override val cells = arrayOf(
            booleanArrayOf(true, true, true, false),
            booleanArrayOf(false, false, true, false),
            booleanArrayOf(false, false, true, false),
            booleanArrayOf(false, false, false, false),
        )
    }

    object Vert : Shape {
        override val cells = arrayOf(
            booleanArrayOf(true, false, false, false),
            booleanArrayOf(true, false, false, false),
            booleanArrayOf(true, false, false, false),
            booleanArrayOf(true, false, false, false),
        )
    }

    object Square : Shape {
        override val cells = arrayOf(
            booleanArrayOf(true, true, false, false),
            booleanArrayOf(true, true, false, false),
            booleanArrayOf(false, false, false, false),
            booleanArrayOf(false, false, false, false),
        )
    }

    companion object {
        fun shapeSequence(): Sequence<Shape> = listOf(Horiz, Plus, Angle, Vert, Square).repeatForever()
    }
}

enum class Move {
    Left,
    Right
}

fun getMovesSequence(): Sequence<Move> {
    val moves = aocInput(22, 17).use { it.readText().trim() }.map { when(it) {
        '<' -> Move.Left
        '>' -> Move.Right
        else -> throw IllegalArgumentException("Unknown char $it")
    } }
    return moves.repeatForever()
}

data class Pos(val row: Int, val col: Int)

fun main() {
    // make it easy, pre-allocate the board. 2022 * 4 is max of 8088 cells needed, let's do 10k to be safe
    val board = Array(10_000) { BooleanArray(7) }

    fun collides(shape: Shape, pos: Pos): Boolean {
        if (pos.row < 0 || pos.col < 0) return true // left-bottom aligned
        for (shapeRow in 0..3) {
            for (shapeCol in 0..3) {
                if (pos.col + shapeCol !in 0..6) {
                    if (shape.cells[shapeRow][shapeCol]) {
                        return true
                    } else {
                        continue
                    }
                }
                if (board[pos.row + shapeRow][pos.col + shapeCol] && shape.cells[shapeRow][shapeCol]) {
                    return true
                }
            }
        }
        return false
    }

    // returns the topmost row baked
    fun bake(shape: Shape, pos: Pos): Int {
        var topmost = -1
        for (shapeRow in 0..3) {
            for (shapeCol in 0..3) {
                if (pos.col + shapeCol !in 0..6) continue
                if (shape.cells[shapeRow][shapeCol]) {
                    board[pos.row + shapeRow][pos.col + shapeCol] = true
                    topmost = pos.row + shapeRow
                }
            }
        }
        return topmost
    }

    var topmost = -1
    val shapes = Shape.shapeSequence().iterator()
    val moves = getMovesSequence().iterator()

    fun printBoard() {
        println("\n===== BOARD =====\n")
        for (row in (topmost + 1 downTo 0)) {
            print('|')
            board[row].forEach {
                if (it) print('#') else print('.')
            }
            println('|')
        }
        println("+-------+\n")
    }

    repeat(2022) {
        var pos = Pos(topmost + 4, 2)
        val shape = shapes.next()
        while(true) {
            // jet move
            val jettedPos = when(moves.next()) {
                Move.Left -> pos.copy(col = pos.col - 1)
                Move.Right -> pos.copy(col = pos.col + 1)
            }
            if (!collides(shape, jettedPos)) {
                pos = jettedPos
            }

            // downward move
            val downwardPos = pos.copy(row = pos.row - 1)
            if (collides(shape, downwardPos)) {
                topmost = max(topmost, bake(shape, pos))
                break
            } else {
                pos = downwardPos
            }
        }

        //printBoard()
    }

    println(topmost + 1)
}
