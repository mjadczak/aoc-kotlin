package uk.co.mjdk.aoc24.day09

import uk.co.mjdk.aoc.aoc
import java.util.*

sealed interface Contents {
    val length: Int
}

data class File(val id: Int, override val length: Int) : Contents
data class Space(override val length: Int) : Contents

typealias DiskMap = List<Contents>

fun DiskMap.checksum(): Long {
    var pos = 0
    var sum = 0L
    forEach {
        val origPos = pos
        pos += it.length
        if (it is File) {
            (origPos..<pos).forEach { p -> sum += (p * it.id) }
        }
    }
    return sum
}

fun MutableList<Contents>.trimEnd() {
    while (last() is Space) {
        removeLast()
    }
}

fun DiskMap.render(): String = buildString {
    this@render.forEach {
        val char = when (it) {
            is Space -> '.'
            is File -> it.id.digitToChar()
        }
        repeat(it.length) { append(char) }
    }
}

fun parseInput(input: String): DiskMap = input.windowedSequence(2, 2, true).flatMapIndexed { idx, str ->
    sequence {
        yield(File(idx, str[0].digitToInt()))
        if (str.length > 1) {
            yield(Space(str[1].digitToInt()))
        }
    }
}.toList()


fun main() = aoc(2024, 9, ::parseInput) {
    example("2333133121414131402")

    part1 { diskMap ->
        val workList = diskMap.toCollection(LinkedList())
        // trim any space
        workList.trimEnd()
        top@ while (true) {
            val candidate = workList.removeLast()
            check(candidate is File)
            var remaining = candidate.length
            while (remaining > 0) {
                val pos = workList.indexOfFirst { it is Space }
                if (pos == -1) {
                    workList.add(File(candidate.id, remaining))
                    break@top
                }
                val space = workList[pos]
                check(space is Space)
                if (space.length <= remaining) {
                    workList.removeAt(pos)
                    workList.add(pos, File(candidate.id, space.length))
                    remaining -= space.length
                } else {
                    workList[pos] = Space(space.length - remaining)
                    workList.add(pos, File(candidate.id, remaining))
                    remaining = 0
                }
            }
            workList.trimEnd()
        }
        workList.checksum()
    }
}
