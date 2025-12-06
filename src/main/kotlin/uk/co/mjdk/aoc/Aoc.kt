package uk.co.mjdk.aoc

import me.alllex.parsus.parser.Grammar
import kotlin.time.measureTimedValue

class Aoc<T>(
    private val year: Int,
    private val day: Int,
    private val trimString: Boolean,
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
        fun parsedInput() = parse(aocString(year, day, trimString))
        fun parsedExample() = parse(example?.trim() ?: throw IllegalStateException("Did not provide example"))

        p1?.let {
            println("Part 1:")

            measureTimedValue {
                if (p1Example) it(parsedExample())
                else it(parsedInput())
            }
        }?.let {
            println(it.value)
            println("Computed in ${it.duration}")
            println()
        }

        p2?.let {
            println("Part 2:")

            measureTimedValue {
                if (p2Example) it(parsedExample())
                else it(parsedInput())
            }
        }?.let {
            println(it.value)
            println("Computed in ${it.duration}")
            println()
        }
    }
}

inline fun aoc(year: Int, day: Int, trimString: Boolean = true, block: (Aoc<String>).() -> Unit) {
    Aoc(year, day, trimString) { it }.apply(block).runParts()
}

inline fun <T> aoc(
    year: Int,
    day: Int,
    noinline parse: (String) -> T,
    trimString: Boolean = true,
    block: (Aoc<T>).() -> Unit
) {
    Aoc(year, day, trimString, parse).apply(block).runParts()
}

inline fun <T> aoc(
    year: Int,
    day: Int,
    grammar: Grammar<T>,
    trimString: Boolean = true,
    block: (Aoc<T>).() -> Unit
) {
    aoc(year, day, { input -> grammar.parseOrThrow(input) }, trimString, block)
}
