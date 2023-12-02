package uk.co.mjdk.aoc

class Aoc(private val year: Int, private val day: Int, private val trimNewLine: Boolean) {
    private var p1: ((String) -> Any)? = null
    private var p2: ((String) -> Any)? = null

    fun part1(func: (String) -> Any) {
        check(p1 == null)
        p1 = func
    }

    fun xpart1(func: (String) -> Any) {
        check(p1 == null)
    }

    fun part2(func: (String) -> Any) {
        check(p2 == null)
        p2 = func
    }

    fun xpart2(func: (String) -> Any) {
        check(p2 == null)
    }

    fun runParts() {
        check(p1 != null || p2 != null)
        val input = aocString(year, day, trimNewLine)
        p1?.invoke(input)?.let {
            println("Part 1:\n$it\n")
        }
        p2?.invoke(input)?.let {
            println("Part 2:\n$it\n")
        }
    }
}

inline fun aoc(year: Int, day: Int, trimNewLine: Boolean = true, block: (Aoc).() -> Unit) {
    Aoc(year, day, trimNewLine).apply(block).runParts()
}
