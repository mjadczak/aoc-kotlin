package uk.co.mjdk.aoc22.day05

import uk.co.mjdk.aoc.aocInput

fun main() {
    val inputStr = aocInput(22, 5).use { it.readText() }
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
    for (line in instrStr.lineSequence()) {
        val match = pat.matchEntire(line) ?: continue
        val (numMove, fromIdx, toIdx) = match.groupValues.drop(1).map(Integer::parseInt)
        repeat(numMove) {
            stacks[toIdx - 1].addFirst(stacks[fromIdx - 1].removeFirst())
        }
    }

    val msg = stacks.map { it.first() }.joinToString(separator = "")
    println(msg)
}
