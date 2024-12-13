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

    part2 { diskMap ->
        val workList = diskMap.toCollection(LinkedList())
        // trim any space
        workList.trimEnd()
        var nextId = workList.last().let { check(it is File); it.id }
//        println(workList.render())
        while (nextId >= 0) {
            val candidatePos = workList.indexOfLast { it is File && it.id == nextId }
            nextId--;
            val candidate = workList[candidatePos]
            check(candidate is File)
            val pos = workList.indexOfFirst { it is Space && it.length >= candidate.length }
            if (pos == -1 || pos >= candidatePos) {
                continue
            }
            run {
                var space = candidate.length
                workList.getOrNull(candidatePos + 1)?.takeIf { it is Space }?.length?.also {
                    space += it
                    workList.removeAt(candidatePos + 1)
                }
                workList.getOrNull(candidatePos - 1)?.takeIf { it is Space }?.length.also {
                    if (it == null) {
                        workList[candidatePos] = Space(space)
                    } else {
                        space += it
                        workList.removeAt(candidatePos)
                        workList[candidatePos - 1] = Space(space)
                    }
                }
            }
            val space = workList[pos]
            check(space is Space)
            if (space.length == candidate.length) {
                workList[pos] = candidate
            } else {
                workList[pos] = Space(space.length - candidate.length)
                workList.add(pos, candidate)
            }
            workList.trimEnd()
//            println(workList.render())
        }
        workList.checksum()
    }
}
