package uk.co.mjdk.aoc

class Aoc<T>(
    private val year: Int,
    private val day: Int,
    private val trimNewLine: Boolean,
    private val parse: (String) -> T
) {
    private var p1: ((T) -> Any)? = null
    private var p1Example = false
    private var p2: ((T) -> Any)? = null
    private var p2Example = false
    private var example: String? = null

    fun example(ex: String) {
        check(example == null)
        example = ex
    }

    fun part1(func: (T) -> Any) {
        check(p1 == null)
        p1 = func
    }

    fun xpart1(func: (T) -> Any) {
        check(p1 == null)
    }

    fun epart1(func: (T) -> Any) {
        check(p1 == null)
        part1(func)
        p1Example = true
    }

    fun part2(func: (T) -> Any) {
        check(p2 == null)
        p2 = func
    }

    fun xpart2(func: (T) -> Any) {
        check(p2 == null)
    }

    fun epart2(func: (T) -> Any) {
        check(p2 == null)
        part2(func)
        p2Example = true
    }

    fun runParts() {
        check(p1 != null || p2 != null)
        // TODO sometimes we want to e.g. return a sequence from the parse function, and then this doesn't work so good!
        val parsedInput by lazy { parse(aocString(year, day, trimNewLine)) }
        val parsedExample by lazy { parse(example?.trim() ?: throw IllegalStateException("Did not provide example")) }

        p1?.let {
            if (p1Example) it(parsedExample)
            else it(parsedInput)
        }?.let {
            println("Part 1:\n$it\n")
        }

        p2?.let {
            if (p2Example) it(parsedExample)
            else it(parsedInput)
        }?.let {
            println("Part 2:\n$it\n")
        }
    }
}

inline fun aoc(year: Int, day: Int, trimNewLine: Boolean = true, block: (Aoc<String>).() -> Unit) {
    Aoc(year, day, trimNewLine) { it }.apply(block).runParts()
}

inline fun <T> aoc(
    year: Int,
    day: Int,
    noinline parse: (String) -> T,
    trimNewLine: Boolean = true,
    block: (Aoc<T>).() -> Unit
) {
    Aoc(year, day, trimNewLine, parse).apply(block).runParts()
}
