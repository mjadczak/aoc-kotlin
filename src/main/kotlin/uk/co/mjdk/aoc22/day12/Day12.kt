package uk.co.mjdk.aoc22.day12

import uk.co.mjdk.aoc.aocInput
import java.util.PriorityQueue

data class Pos(val row: Int, val col: Int) {
    fun neighbours(): Sequence<Pos> = sequenceOf(
        Pos(row + 1, col),
        Pos(row - 1, col),
        Pos(row, col + 1),
        Pos(row, col - 1),
    )
}

data class Cell(val pos: Pos, val height: Int, var visited: Boolean = false, var distance: Int = Int.MAX_VALUE)

operator fun <T> List<List<T>>.get(pos: Pos): T? =
    if (pos.row >= this.size || pos.row < 0 || pos.col >= this[0].size || pos.col < 0) null else this[pos.row][pos.col]

data class Input(val start: Pos, val end: Pos, val grid: List<List<Cell>>)

fun parseInput(): Input {
    aocInput(22, 12).useLines { lines ->
        var start: Pos? = null
        var end: Pos? = null
        val grid = lines.withIndex().map { (row, line) ->
            line.withIndex().map { (col, c) ->
                var cx = c
                when (cx) {
                    'S' -> {
                        assert(start == null)
                        start = Pos(row, col)
                        cx = 'a'
                    }

                    'E' -> {
                        assert(end == null)
                        end = Pos(row, col)
                        cx = 'z'
                    }
                }

                Cell(Pos(row, col), (cx - 'a'))
            }.toList()
        }.toList()

        return Input(start!!, end!!, grid)
    }
}

fun main() {
    val (start, end, grid) = parseInput()
    grid[start]!!.distance = 0
    var current = grid[start]!!
    val destination = grid[end]!!
    val pq = PriorityQueue<Cell>(grid.size * grid[0].size, Comparator.comparingInt { it.distance })
    pq.add(current)
    while (!destination.visited) {
        current.pos.neighbours().map(grid::get).filterNotNull().filterNot(Cell::visited)
            .filter { (it.height - current.height) <= 1 }.forEach { neighbour ->
                val tentativeDistance = current.distance + 1
                if (tentativeDistance < neighbour.distance) {
                    neighbour.distance = tentativeDistance
                    // This is not great algorithmically, but yet again I'm too lazy to implement a queue with increase-priority
                    pq.remove(neighbour)
                    pq.add(neighbour)
                }
            }
        current.visited = true
        current = pq.remove()
    }

    println(destination.distance)
}
