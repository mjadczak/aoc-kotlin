package uk.co.mjdk.aoc23.day08

import uk.co.mjdk.aoc.aoc
import uk.co.mjdk.aoc.repeatForever

private data class Node(val id: String, val left: String, val right: String) {
    operator fun get(direction: Direction): String = when (direction) {
        Direction.L -> left
        Direction.R -> right
    }

    companion object {
        fun fromLine(line: String): Node {
            // should just import a PEG library for these
            // 0123456789012345
            // BRR = (LVC, FSJ)
            require(line.length == 16)
            return Node(line.slice(0..2), line.slice(7..9), line.slice(12..14))
        }
    }
}

private enum class Direction {
    R, L;

    companion object {
        fun valueOf(char: Char): Direction = when (char) {
            'R' -> R
            'L' -> L
            else -> throw IllegalArgumentException(char.toString())
        }
    }
}

private fun parse(input: String): Pair<List<Direction>, Map<String, Node>> {
    val seq = input.lineSequence()
    val directions = seq.first().map(Direction::valueOf)
    val nodes = seq.drop(2).map(Node::fromLine).associateBy { it.id }

    return directions to nodes
}

private data class Cycle(val offset: Int, val period: Int)

// use the "tortoise and hare" algorithm to detect the cycle - probably no need to get more fancy
// https://www.wikiwand.com/en/Cycle_detection#Floyd's_tortoise_and_hare
// for a general purpose function we probably don't want to assume there is a cycle!
private fun <T> detectCycle(start: T, next: (T) -> T): Cycle {
    fun (T).next(): T = next(this)

    var tortoise = start.next()
    var hare = start.next().next()
    while (tortoise != hare) {
        tortoise = tortoise.next()
        hare = hare.next().next()
    }

    var offset = 0
    tortoise = start
    while (tortoise != hare) {
        tortoise = tortoise.next()
        hare = hare.next()
        offset += 1
    }

    var period = 1
    hare = tortoise.next()
    while (tortoise != hare) {
        hare = hare.next()
        period += 1
    }

    return Cycle(offset, period)
}

fun main() = aoc(2023, 8, ::parse) {
    part1 { (directions, nodes) ->
        directions.repeatForever().runningFold("AAA") { node, direction ->
            nodes[node]!![direction]
        }.withIndex().first { it.value == "ZZZ" }.index
    }

    part2 { (directions, nodes) ->
        val starting = nodes.keys.filter { it[2] == 'A' }

        data class RepeatingPath(
            val cycle: Cycle,
            val endNodesBeforeStart: List<Long>,
            val endNodeCycleOffsets: List<Long>
        )

        fun condensePath(startNode: String): RepeatingPath {
            // every node in the input has a left and right child node (which is also in the graph),
            // so we _must_ have cycles since our directions list is finite
            // find the cycle offset, and then all repeating end node offsets from there
            // for a cycle, we need to be at the same index in the directions as well as on the same node
            // we also grab all the offsets at which we are on an end node before we reach the start of the cycle

            data class Pos(val node: String, val dirIndex: Int) {
                fun next(): Pos {
                    val dir = directions[dirIndex]
                    val nextIdx = (dirIndex + 1) % directions.size
                    val nextNode = nodes[node]!![dir]
                    return Pos(nextNode, nextIdx)
                }
            }

            val startPos = Pos(startNode, 0)

            val cycle = detectCycle(startPos) { it.next() }

            // now find offsets of end nodes
            // we could do this in one pass but meh
            val posSeq = generateSequence(startPos) { it.next() }
            val beforeEndNodes =
                posSeq.take(cycle.offset)
                    .mapIndexedNotNull { idx, pos -> if (pos.node[2] == 'Z') idx.toLong() else null }
                    .toList()
            val afterEndNodes =
                posSeq.drop(cycle.offset).take(cycle.period)
                    .mapIndexedNotNull { idx, pos -> if (pos.node[2] == 'Z') idx.toLong() else null }
                    .toList()

            return RepeatingPath(cycle, beforeEndNodes, afterEndNodes)
        }

        val repeatingPaths = starting.map(::condensePath)
        // in practice, there are no repeating nodes before the start in the inputs given, but it's nice to be
        // able to consider them so as not to make too many assumptions about the input :)
//        check(repeatingPaths.all { it.endNodesBeforeStart.isEmpty() })
        // and look! it turns out that there is only a single end node inside each cycle
        // I'm sure they don't even overlap, how nice of them (though we don't care if they do, at this point)
        // stick the assertion in for fun but we actually end up handling the case where there are more
        // note the example doesn't actually have this property
//        check(repeatingPaths.all { it.endNodeCycleOffsets.size == 1 })

        // So now that we have a nice way of checking straight from a given index whether all the paths
        // are on an end node, and a nice way of skipping forward by many steps, we just find the lowest index
        // where this happens. There might be some sort of closed-form solution based on LCMs or something?

        // But that seems like too much effort, let's just do it with a loop: at each point we check each path
        // to see what number of steps we need to take to get it to an end node.
        // We then advance by the _largest_ such number since there's no point in checking the smaller ones

        // if these weren't always lists of size 0 and 1 we could take advantage of the fact they're sorted
        fun RepeatingPath.isAtEnd(idx: Long): Boolean =
            if (idx < cycle.offset) {
                idx in endNodesBeforeStart
            } else {
                ((idx - cycle.offset) % cycle.period) in endNodeCycleOffsets
            }

        fun RepeatingPath.stepsToNextEnd(idx: Long): Long {
            // find our current position in the cycle - want a negative number if we haven't entered yet
            val cyclePos = if (idx < cycle.offset) {
                // return early if there's a pre-cycle end node
                endNodesBeforeStart.firstOrNull { it > idx }?.let { return idx - it }
                idx - cycle.offset
            } else {
                (idx - cycle.offset) % cycle.period
            }

            val nextCyclePos =
                endNodeCycleOffsets.firstOrNull { it > cyclePos } ?: (cycle.period + endNodeCycleOffsets.first())
            return nextCyclePos - cyclePos
        }

        // In practice, because of the properties of the input, we always end up skipping ahead by the largest of
        // the periods, and so could likely optimise this further with some sort of LCM calculation
        // But, this is fast-ish enough and I don't need to think if that would work for the general case
        generateSequence(0L) { idx ->
            idx + repeatingPaths.maxOf { it.stepsToNextEnd(idx) }
        }.first { idx -> repeatingPaths.all { it.isAtEnd(idx) } }
    }
}
