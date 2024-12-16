package uk.co.mjdk.aoc24.day10

import uk.co.mjdk.aoc.aoc

data class Coord(val row: Int, val col: Int)

class Board(private val data: IntArray, val rows: Int) {
    val cols: Int = run {
        require(data.size % rows == 0)
        data.size / rows
    }

    val trailheads = buildSet {
        (0..<rows).forEach { row ->
            (0..<cols).forEach { col ->
                Coord(row, col).let { coord ->
                    if (this@Board[coord] == 0) {
                        add(coord)
                    }
                }
            }
        }
    }

    operator fun contains(coord: Coord): Boolean = coord.row in 0..<rows && coord.col in 0..<cols
    operator fun get(coord: Coord): Int? = if (contains(coord)) data[cols * coord.row + coord.col] else null

    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            val data = lines.flatMap { l ->
                l.map { c -> c.digitToInt() }
            }.toIntArray()
            return Board(data, lines.size)
        }
    }
}

private class Computation(val board: Board) {
    private val memo = mutableMapOf<Coord, Set<Coord>>()
    private fun compute(coord: Coord): Set<Coord> {
        val height = board[coord] ?: throw IllegalArgumentException(coord.toString())
        check(height <= 9)
        if (height == 9) return setOf(coord)

        return listOf(
            coord.copy(row = coord.row + 1),
            coord.copy(row = coord.row - 1),
            coord.copy(col = coord.col + 1),
            coord.copy(col = coord.col - 1),
        ).mapNotNull { candCoord ->
            board[candCoord]?.takeIf { it == height + 1 }?.let {
                computeOrGet(candCoord)
            }
        }.fold(emptySet()) { a, b -> a.union(b) }
    }

    fun computeOrGet(coord: Coord): Set<Coord> = memo[coord] ?: compute(coord).also { memo[coord] = it }
}

private class Computation2(val board: Board) {
    private val memo = mutableMapOf<Coord, Set<List<Coord>>>()
    private fun compute(coord: Coord): Set<List<Coord>> {
        val height = board[coord] ?: throw IllegalArgumentException(coord.toString())
        check(height <= 9)
        if (height == 9) return setOf(listOf(coord))

        return listOf(
            coord.copy(row = coord.row + 1),
            coord.copy(row = coord.row - 1),
            coord.copy(col = coord.col + 1),
            coord.copy(col = coord.col - 1),
        ).mapNotNull { candCoord ->
            board[candCoord]?.takeIf { it == height + 1 }?.let {
                computeOrGet(candCoord).mapTo(mutableSetOf()) { list -> list + coord }
            }
        }.fold(emptySet()) { a, b -> a.union(b) }
    }

    fun computeOrGet(coord: Coord): Set<List<Coord>> = memo[coord] ?: compute(coord).also { memo[coord] = it }
}

fun main() = aoc(2024, 10, Board::parse) {
    example(
        """
        89010123
        78121874
        87430965
        96549874
        45678903
        32019012
        01329801
        10456732
    """.trimIndent()
    )

    part1 { board ->
        val computation = Computation(board)
        board.trailheads.sumOf { t -> computation.computeOrGet(t).size }
    }

    part2 { board ->
        val computation = Computation2(board)
        board.trailheads.sumOf { t -> computation.computeOrGet(t).size }
    }
}
