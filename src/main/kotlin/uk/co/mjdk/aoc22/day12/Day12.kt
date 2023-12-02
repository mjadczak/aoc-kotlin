package uk.co.mjdk.aoc22.day12

import uk.co.mjdk.aoc.aocReader
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
    aocReader(22, 12).useLines { lines ->
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

fun part1() {
    val (start, end, grid) = parseInput()
    grid[start]!!.distance = 0
    val destination = grid[end]!!
    val pq = PriorityQueue<Cell>(grid.size * grid[0].size, Comparator.comparingInt { it.distance })
    pq.add(grid[start]!!)
    while (!destination.visited) {
        val current = pq.remove()
        current.pos.neighbours().map(grid::get).filterNotNull().filterNot(Cell::visited)
            .filter { (it.height - current.height) <= 1 }.forEach { neighbour ->
                val tentativeDistance = current.distance + 1
                if (tentativeDistance < neighbour.distance) {
                    neighbour.distance = tentativeDistance
                    // We actually don't need to remove it, as it will always languish at the end
                    // pq.remove(neighbour)
                    pq.add(neighbour)
                }
            }
        current.visited = true
    }

    println(destination.distance)
}

fun part2() {
    val (_, start, grid) = parseInput() // end -> start
    grid[start]!!.distance = 0
    val aDistances = mutableListOf<Int>()
    val pq = PriorityQueue<Cell>(grid.size * grid[0].size, Comparator.comparingInt { it.distance })
    pq.add(grid[start]!!)
    while (!pq.isEmpty()) {
        val current = pq.remove()
        current.pos.neighbours().map(grid::get).filterNotNull().filterNot(Cell::visited)
            .filter { (current.height - it.height) <= 1 }.forEach { neighbour ->
                val tentativeDistance = current.distance + 1
                if (tentativeDistance < neighbour.distance) {
                    neighbour.distance = tentativeDistance
                    pq.remove(neighbour)
                    pq.add(neighbour)
                }
            }
        current.visited = true
        if (current.height == 0) {
            aDistances.add(current.distance)
        }
    }

    println(aDistances.min())
}

fun main() {
    part1()
    part2()
}
