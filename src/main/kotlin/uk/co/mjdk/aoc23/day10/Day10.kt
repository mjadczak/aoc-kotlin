package uk.co.mjdk.aoc23.day10

import uk.co.mjdk.aoc.aoc
import java.util.EnumSet

private enum class Dir {
    North,
    East,
    South,
    West;

    val corresponding: Dir
        get() = when (this) {
            North -> South
            South -> North
            East -> West
            West -> East
        }
}

private enum class Pipe(val outboundDirections: Set<Dir>) {
    NS(EnumSet.of(Dir.North, Dir.South)),
    EW(EnumSet.of(Dir.East, Dir.West)),
    NE(EnumSet.of(Dir.North, Dir.East)),
    NW(EnumSet.of(Dir.North, Dir.West)),
    SW(EnumSet.of(Dir.South, Dir.West)),
    SE(EnumSet.of(Dir.South, Dir.East));

    fun accepts(inbound: Dir): Boolean = inbound.corresponding in outboundDirections
    fun nextOutbound(inbound: Dir): Dir = outboundDirections.find { it != inbound.corresponding }
        ?: throw IllegalStateException("No connection when going $inbound to this pipe")

    companion object {
        fun parse(input: Char): Pipe = when (input) {
            '|' -> NS
            '-' -> EW
            'L' -> NE
            'J' -> NW
            '7' -> SW
            'F' -> SE
            else -> throw IllegalArgumentException(input.toString())
        }
    }
}

private data class Coord(val row: Int, val col: Int) {
    operator fun plus(dir: Dir): Coord = when (dir) {
        Dir.North -> copy(row = row - 1)
        Dir.South -> copy(row = row + 1)
        Dir.West -> copy(col = col - 1)
        Dir.East -> copy(col = col + 1)
    }

    val outbound: List<Pair<Dir, Coord>>
        get() = Dir.entries.map { it to this + it }
}

private class Board(input: String) {
    private val cells: Array<Array<Pipe?>>
    val start: Coord

    init {
        var s: Coord? = null
        val lines = input.lines()
        cells = Array(lines.size) { row ->
            val line = lines[row]
            Array(line.length) { col ->
                val char = line[col]
                val coord = Coord(row, col)
                when (char) {
                    'S' -> {
                        check(s == null)
                        s = coord
                        null
                    }

                    '.' -> null
                    else -> Pipe.parse(char)
                }
            }
        }

        s.let {
            check(it != null)
            start = it
        }

        // Question says that S will have exactly two pipes connected to it - so we can determine what pipe it must be
        val connected = start.outbound.mapNotNull { (outboundDir, coord) ->
            this[coord]?.takeIf { it.accepts(outboundDir) }?.let { outboundDir }
        }
        check(connected.size == 2)
        val pipeSet = EnumSet.of(connected[0], connected[1])
        val startPipe = Pipe.entries.find { it.outboundDirections == pipeSet } ?: throw IllegalStateException()
        this[start] = startPipe
    }

    operator fun get(coord: Coord): Pipe? = cells[coord.row][coord.col]
    fun getPipe(coord: Coord): Pipe = get(coord) ?: throw IllegalStateException("No pipe at $coord")
    private operator fun set(coord: Coord, value: Pipe) {
        cells[coord.row][coord.col] = value
    }
}

fun main() = aoc(2023, 10, { Board(it) }) {
    part1 { board ->
        data class MoveState(val coord: Coord, val outboundDir: Dir)
        // pick arbitrary direction to start with
        val initial = MoveState(board.start, board.getPipe(board.start).outboundDirections.first())
        val loopSeq = generateSequence(initial) { st ->
            val nextCoord = st.coord + st.outboundDir
            val nextDir = board.getPipe(nextCoord).nextOutbound(st.outboundDir)
            MoveState(nextCoord, nextDir)
        }

        val loopLength = loopSeq.drop(1).takeWhile { it.coord != initial.coord }.count() + 1

        loopLength / 2
    }
}
