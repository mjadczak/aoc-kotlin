package uk.co.mjdk.aoc25.day07

import uk.co.mjdk.aoc.aoc

data class Coord(val row: Int, val col: Int) {
    val next: Coord get() = copy(row = row + 1)
    val sides: Sequence<Coord> get() = sequenceOf(copy(col = col + 1), copy(col = col - 1))
}

enum class BoardPos {
    Empty,
    Splitter;
}

enum class CellState {
    Lit,
    Unlit;
}

class Setup(val numCols: Int, val start: Coord, private val cells: List<BoardPos>) {
    val numRows: Int = run {
        check(cells.size % numCols == 0)
        cells.size / numCols
    }

    val rowIndices = 0..<numRows
    val colIndices = 0..<numCols

    private val Coord.indexUnsafe: Int get() = row * numCols + col
    private val Coord.index: Int? get() = if (contains(this)) indexUnsafe else null

    operator fun contains(coord: Coord): Boolean = coord.row in rowIndices && coord.col in colIndices
    operator fun get(coord: Coord): BoardPos = getOrNull(coord) ?: throw IndexOutOfBoundsException()
    fun getOrNull(coord: Coord): BoardPos? = coord.index?.let { cells[it] }

    companion object {
        fun parse(input: String): Setup {
            val lines = input.lineSequence()
            val numCols = lines.first().trim().length
            var start: Coord? = null
            val cells = lines.flatMapIndexed { rowIdx, line ->
                line.mapIndexed { colIdx, c ->
                    when (c) {
                        '.' -> BoardPos.Empty
                        '^' -> BoardPos.Splitter
                        'S' -> {
                            check(start == null)
                            start = Coord(rowIdx, colIdx)
                            BoardPos.Empty
                        }

                        else -> error(c)
                    }
                }
            }.toList()
            check(start != null)
            return Setup(numCols, start, cells)
        }
    }
}

class MutableStateTracker(val setup: Setup) {
    private val litCells = mutableSetOf<Coord>()
    private val beams: ArrayDeque<Coord> = ArrayDeque()
    private var numSplit: Int = 0
    val numTimesSplit: Int get() = numSplit

    operator fun get(coord: Coord): CellState? =
        if (coord in setup)
            if (coord in litCells) CellState.Lit else CellState.Unlit
        else null

    fun calculate() {
        check(litCells.isEmpty())
        check(beams.isEmpty())
        check(numSplit == 0)
        beams.addLast(setup.start)
        while (beams.isNotEmpty()) {
            processBeam(beams.removeFirst())
        }
    }

    fun processBeam(beamStart: Coord) {
        var current = beamStart
        while (current in setup) {
            if (!litCells.add(current)) {
                // this path was already processed, so bail
                break
            }
            if (setup[current] == BoardPos.Splitter) {
                // stop processing this beam, and queue up the sides
                current.sides.forEach { beams.addLast(it) }
                numSplit += 1
                break
            }
            current = current.next
        }
    }
}

class MutableQuantumTracker(val setup: Setup) {
    private val cache: MutableMap<Coord, Long> = mutableMapOf()

    private fun numPossibilities(beamStart: Coord): Long =
        cache[beamStart] ?: calcNumPossibilities(beamStart).also { cache[beamStart] = it }

    private fun calcNumPossibilities(beamStart: Coord): Long {
        var current = beamStart
        while (current in setup) {
            if (setup[current] == BoardPos.Splitter) {
                // stop processing this beam, and queue up the sides
                return current.sides.sumOf { numPossibilities(it) }
            }
            current = current.next
        }
        return 1L // we got to the bottom of the board without hitting a splitter
    }

    fun calculate(): Long = numPossibilities(setup.start)
}

fun main() = aoc(2025, 7, Setup::parse) {
    part1 { setup ->
        MutableStateTracker(setup).also { it.calculate() }.numTimesSplit
    }

    part2 { setup ->
        val tracker = MutableQuantumTracker(setup)
        tracker.calculate()
    }
}
