package uk.co.mjdk.aoc22.day05

import uk.co.mjdk.aoc.aocReader

data class Instruction(val numberToMove: Int, val fromIdx: Int, val toIdx: Int)

fun parseInput(): Pair<List<ArrayDeque<Char>>, List<Instruction>> {
    val inputStr = aocReader(22, 5).use { it.readText() }
    val (stackStr, instrStr) = inputStr.split("\n\n")
    val stackLines = stackStr.lines()
    val last = stackLines.last()
    val len = last.length
    assert(len % 4 == 3)
    val numStacks = (len + 1) / 4
    assert(last[len - 2] == numStacks.digitToChar())
    val stacks = List(numStacks) { ArrayDeque<Char>() }
    stackLines.asReversed().asSequence().drop(1).forEach { line ->
        for (i in 0 until numStacks) {
            val charPos = 1 + i * 4
            if (charPos >= line.length || !line[charPos].isUpperCase()) {
                continue
            }
            stacks[i].addFirst(line[charPos])
        }
    }

    val pat = Regex("""move (\d+) from (\d) to (\d)""")
    val instrs = instrStr.lines().filterNot(String::isBlank).map {
        val match = pat.matchEntire(it)!!
        val (numMove, fromIdx, toIdx) = match.groupValues.drop(1).map(Integer::parseInt)
        Instruction(numMove, fromIdx - 1, toIdx - 1)
    }

    return stacks to instrs
}

fun part1() {
    val (stacks, instructions) = parseInput()
    instructions.forEach { instr ->
        repeat(instr.numberToMove) {
            stacks[instr.toIdx].addFirst(stacks[instr.fromIdx].removeFirst())
        }
    }

    val msg = stacks.map { it.first() }.joinToString(separator = "")
    println(msg)
}

fun part2() {
    val (stacks, instructions) = parseInput()
    instructions.forEach { instr ->
        val crane = ArrayDeque<Char>(instr.numberToMove)
        repeat(instr.numberToMove) {
            crane.addFirst(stacks[instr.fromIdx].removeFirst())
        }
        repeat(instr.numberToMove) {
            stacks[instr.toIdx].addFirst(crane.removeFirst())
        }
    }

    val msg = stacks.map { it.first() }.joinToString(separator = "")
    println(msg)
}

fun main() {
    part1()

    part2()
}
