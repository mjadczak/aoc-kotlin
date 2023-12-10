package uk.co.mjdk.aoc23.day10

import uk.co.mjdk.aoc.aoc
import java.util.EnumSet

private enum class Dir {
    North, East, South, West;

    val corresponding: Dir
        get() = when (this) {
            North -> South
            South -> North
            East -> West
            West -> East
        }
}

private enum class Pipe(val outboundDirections: Set<Dir>) {
    NS(EnumSet.of(Dir.North, Dir.South)), EW(EnumSet.of(Dir.East, Dir.West)), NE(EnumSet.of(Dir.North, Dir.East)), NW(
        EnumSet.of(Dir.North, Dir.West)
    ),
    SW(EnumSet.of(Dir.South, Dir.West)), SE(EnumSet.of(Dir.South, Dir.East));

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
    val rows: Int
        get() = cells.size
    val cols: Int
        get() = cells[0].size

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
        val connected = start.outbound.filter { contains(it.second) }.mapNotNull { (outboundDir, coord) ->
            this[coord]?.takeIf { it.accepts(outboundDir) }?.let { outboundDir }
        }
        check(connected.size == 2)
        val pipeSet = EnumSet.of(connected[0], connected[1])
        val startPipe = Pipe.entries.find { it.outboundDirections == pipeSet } ?: throw IllegalStateException()
        this[start] = startPipe
    }

    operator fun get(coord: Coord): Pipe? = cells[coord.row][coord.col]
    fun getPipe(coord: Coord): Pipe = get(coord) ?: throw IllegalStateException("No pipe at $coord")
    fun contains(coord: Coord): Boolean = coord.row in 0..<rows && coord.col in 0..<cols
    private operator fun set(coord: Coord, value: Pipe) {
        cells[coord.row][coord.col] = value
    }
}

private sealed interface ScanState

private data object Outside : ScanState
private data object Inside : ScanState
private data class InPipe(val initialVerticalDir: Dir, val prevState: ScanState) : ScanState {
    init {
        require(initialVerticalDir == Dir.North || initialVerticalDir == Dir.South)
    }
}

private fun ScanState.flipped(): ScanState = when (this) {
    Inside -> Outside
    Outside -> Inside
    else -> throw IllegalStateException("Cannot flip $this")
}

fun main() = aoc(2023, 10, { Board(it) }) {
    fun Board.loopSeq(): Sequence<Coord> {
        data class MoveState(val coord: Coord, val outboundDir: Dir)
        // pick arbitrary direction to start with
        // start point ends up at end, but that's fine
        val initial = MoveState(this.start, this.getPipe(this.start).outboundDirections.first())
        return generateSequence(initial) { st ->
            val nextCoord = st.coord + st.outboundDir
            val nextDir = this.getPipe(nextCoord).nextOutbound(st.outboundDir)
            MoveState(nextCoord, nextDir)
        }.map { it.coord }.drop(1).takeWhile { it != this.start }.plusElement(this.start)
    }

    part1 { board ->
        board.loopSeq().count() / 2
    }

    part2 { board ->
        val pipes = board.loopSeq().associateWith { board.getPipe(it) }
        val upDown = EnumSet.of(Dir.North, Dir.South)

        (0..<board.rows).sumOf { row ->
            // essentially split up the row into segments of inside and outside
            // but for horizontal runs we need to care about whether we end up exiting in the same direction or not

            var state: ScanState = Outside
            var insideArea = 0

            for (col in 0..<board.cols) {
                val coord = Coord(row, col)
                val pipe = pipes[coord]
                if (pipe == null) {
                    if (state == Inside) {
                        insideArea += 1
                    }
                } else {
                    val verticals = pipe.outboundDirections.intersect(upDown)
                    state = when (verticals.size) {
                        // horizontal pipe
                        0 -> state.also { check(it is InPipe) }
                        // vertical pipe
                        2 -> state.flipped()
                        // corner pipe
                        1 -> when (val s = state) {
                            is InPipe -> {
                                if (s.initialVerticalDir == verticals.first()) {
                                    // u-turn, no state change
                                    s.prevState
                                } else {
                                    // staggered vertical pipe, change
                                    s.prevState.flipped()
                                }
                            }

                            else -> InPipe(verticals.first(), s)
                        }

                        else -> throw IllegalStateException(verticals.toString())
                    }
                }
            }

            insideArea
        }
    }
}
