package uk.co.mjdk.aoc22.day17

import uk.co.mjdk.aoc.aocReader
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
        fun shapeList(): List<Shape> = listOf(Horiz, Plus, Angle, Vert, Square)
    }
}

enum class Move {
    Left,
    Right
}

fun getMovesList(): List<Move> = aocReader(22, 17).use { it.readText().trim() }.map {
    when (it) {
        '<' -> Move.Left
        '>' -> Move.Right
        else -> throw IllegalArgumentException("Unknown char $it")
    }
}

data class Pos(val row: Int, val col: Int)

class Board(size: Int) {
    private val board = Array(size) { BooleanArray(7) }

    var topmost: Int = -1
        private set

    var numRocks: Int = 0
        private set

    val height: Int
        get() = topmost + 1

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

    fun bake(shape: Shape, pos: Pos) {
        var topmostRow = -1
        for (shapeRow in 0..3) {
            for (shapeCol in 0..3) {
                if (pos.col + shapeCol !in 0..6) continue
                if (shape.cells[shapeRow][shapeCol]) {
                    board[pos.row + shapeRow][pos.col + shapeCol] = true
                    topmostRow = pos.row + shapeRow
                }
            }
        }
        topmost = max(topmost, topmostRow)
        numRocks += 1
    }

    operator fun get(row: Int, col: Int): Boolean = board[row][col]

    fun simulate(nextShape: () -> Shape, nextMove: () -> Move) {
        var pos = Pos(topmost + 4, 2)
        val shape = nextShape()
        while (true) {
            // jet move
            val jettedPos = when (nextMove()) {
                Move.Left -> pos.copy(col = pos.col - 1)
                Move.Right -> pos.copy(col = pos.col + 1)
            }
            if (!collides(shape, jettedPos)) {
                pos = jettedPos
            }

            // downward move
            val downwardPos = pos.copy(row = pos.row - 1)
            if (collides(shape, downwardPos)) {
                bake(shape, pos)
                break
            } else {
                pos = downwardPos
            }
        }
    }

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
}

fun part1() {
    // make it easy, pre-allocate the board. 2022 * 4 is max of 8088 cells needed, let's do 10k to be safe
    val board = Board(10_000)

    val shapes = Shape.shapeList().repeatForever().iterator()
    val moves = getMovesList().repeatForever().iterator()

    while (board.numRocks < 2022) {
        board.simulate(shapes::next, moves::next)
    }

    println(board.height)
}

data class CacheKey(val shapeIndex: Int, val jetIndex: Int, val surface: IntArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CacheKey

        if (shapeIndex != other.shapeIndex) return false
        if (jetIndex != other.jetIndex) return false
        if (!surface.contentEquals(other.surface)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shapeIndex
        result = 31 * result + jetIndex
        result = 31 * result + surface.contentHashCode()
        return result
    }
}

data class CacheValue(val numRocks: Int, val topmost: Int)

fun part2() {
    // Run simulation while caching the state of the surface, index of shape and index of jetstream.
    // Cross our fingers that we detect a cycle - at that point fast-forward until the end of the simulation, then
    // do the last bit explicitly.

    val numRocksRequired = 1_000_000_000_000L

    val board = Board(32_000_000) // hope the cycle is shorter than this

    val cache = HashMap<CacheKey, CacheValue>()
    val shapes = Shape.shapeList()
    val moves = getMovesList()
    var shapeIdx = 0
    var moveIdx = 0
    fun nextShape(): Shape {
        val shape = shapes[shapeIdx]
        shapeIdx = (shapeIdx + 1) % shapes.size
        return shape
    }

    fun nextMove(): Move {
        val move = moves[moveIdx]
        moveIdx = (moveIdx + 1) % moves.size
        return move
    }

    fun currentCacheKey(): CacheKey {
        val surface = IntArray(7)
        for (col in 0..6) {
            surface[col] = (board.topmost downTo 0).firstOrNull { board[it, col] } ?: 0
        }
        val base = surface.min()
        for (col in 0..6) {
            surface[col] -= base
        }
        return CacheKey(shapeIdx, moveIdx, surface)
    }

    while (true) {
        val key = currentCacheKey()
        if (key in cache) {
            break
        }
        cache[currentCacheKey()] = CacheValue(board.numRocks, board.topmost)
        board.simulate(::nextShape, ::nextMove)
    }

    // Yay, we found a cycle. Calculate how many cycles we can get for free, and then restart the simulation to finish off
    val key = currentCacheKey()
    val value = cache[key]!!
    val rocksLeft = numRocksRequired - board.numRocks
    val cycleLength = board.numRocks - value.numRocks
    val cycleAdvance = board.topmost - value.topmost
    val numCycles = rocksLeft / cycleLength
    val extraHeight = cycleAdvance.toLong() * numCycles
    val remainingRocks = numRocksRequired - board.numRocks - (numCycles * cycleLength)

    repeat(remainingRocks.toInt()) {
        board.simulate(::nextShape, ::nextMove)
    }

    println(extraHeight + board.height)
}

fun main() {
    part1()
    part2()
}
