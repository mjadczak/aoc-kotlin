package uk.co.mjdk.aoc24.day12

import uk.co.mjdk.aoc.aoc

data class Coord(val row: Int, val col: Int) {
    val adjacent: List<Coord>
        get() = listOf(
            this.copy(row = this.row + 1),
            this.copy(row = this.row - 1),
            this.copy(col = this.col + 1),
            this.copy(col = this.col - 1),
        )
}

class Board(private val data: CharArray, val rows: Int) {
    val cols: Int = run {
        require(data.size % rows == 0)
        data.size / rows
    }

    operator fun contains(coord: Coord): Boolean = coord.row in (0..<rows) && coord.col in (0..<cols)

    operator fun get(coord: Coord): Char? = if (contains(coord)) data[coord.toIdx()] else null

    private fun Coord.toIdx(): Int = row * cols + col
    private fun Int.toCoord(): Coord = Coord(this / cols, this % cols)

    data class Region(val plant: Char, val coords: Set<Coord>) {
        fun merge(other: Region): Region {
            require(other.plant == this.plant)
            return copy(coords = coords + other.coords)
        }
    }

    private val regionCells: Array<Region>

    init {
        val regionData = Array<Region?>(data.size) { null }

        data.indices.forEach { i ->
            val char = data[i]
            val coord = i.toCoord()

            val adjacent =
                coord.adjacent.filter { this[it] == char }.mapNotNull { c -> regionData[c.toIdx()]?.let { c to it } }

            val thisRegion = adjacent.map { it.second }.plus(Region(char, setOf(coord))).reduce(Region::merge)
            thisRegion.coords.forEach { regionData[it.toIdx()] = thisRegion }
        }

        regionCells = regionData.map { region -> requireNotNull(region) }.toTypedArray()
    }

    val regions: Set<Region> = regionCells.toSet()
    fun region(coord: Coord): Region? = if (contains(coord)) regionCells[coord.toIdx()] else null

    companion object {
        fun parse(input: String): Board {
            val lines = input.lines()
            val data = lines.flatMap { it.asSequence() }.toCharArray()
            return Board(data, lines.size)
        }
    }
}

enum class Side(val horizontal: Boolean) {
    Top(true), Bottom(true), Left(false), Right(false)
}

fun main() = aoc(2024, 12, Board::parse) {
    example(
        """
            OOOOO
            OXOXO
            OOOOO
            OXOXO
            OOOOO
    """.trimIndent()
    )

    part1 { board ->
        board.regions.sumOf { region ->
            val area = region.coords.size
            val perimeter = region.coords.sumOf { coord ->
                coord.adjacent.count { board.region(it) != region }
            }
            area * perimeter
        }
    }

    part2 { board ->
        board.regions.sumOf { region ->
            val area = region.coords.size
            val perimeterSides = region.coords.flatMap { coord ->
                listOfNotNull(
                    coord.copy(row = coord.row - 1).takeIf { board.region(it) != region }
                        ?.let { coord to Side.Top },
                    coord.copy(row = coord.row + 1).takeIf { board.region(it) != region }
                        ?.let { coord to Side.Bottom },
                    coord.copy(col = coord.col - 1).takeIf { board.region(it) != region }
                        ?.let { coord to Side.Left },
                    coord.copy(col = coord.col + 1).takeIf { board.region(it) != region }
                        ?.let { coord to Side.Right },
                )
            }
            val sides = perimeterSides.groupBy(
                { it.second to (if (it.second.horizontal) it.first.row else it.first.col) },
                { if (it.second.horizontal) it.first.col else it.first.row }).values.sumOf { vs ->
                vs.sorted().zipWithNext { a, b -> b != a + 1 }.count { it } + 1
            }
            area * sides
        }
    }
}
