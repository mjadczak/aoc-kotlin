package uk.co.mjdk.aoc25.day02

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import uk.co.mjdk.aoc.aoc

fun Long.numDigits(): Int {
    var res = 0
    var num = this
    while (num != 0L) {
        num /= 10
        res += 1
    }
    return res
}

fun pow10(power: Int): Long {
    check(power >= 0)
    var res = 1L
    var pow = power
    while (pow > 0) {
        res *= 10
        pow -= 1
    }
    return res
}

fun Int.factors(): Sequence<Int> {
    require(this > 0)
    val num = this
    return (1..num).asSequence().filter { num % it == 0 }
}

fun Long.groupings(digits: Int): Sequence<Long> {
    require(digits > 0)
    var num = this
    require(num > 0)
    val divisor = pow10(digits)
    return sequence {
        while (num > 0) {
            yield(num % divisor)
            num /= divisor
        }

    }
}

fun <T : Any> Sequence<T>.allEqual(): Boolean {
    var initial: T? = null
    for (el in this) {
        if (initial == null) {
            initial = el
            continue
        }
        if (initial != el) {
            return false
        }
    }
    return true
}

val grammar = object : Grammar<List<LongRange>>() {
    val long by regexToken("[0-9]+").map { it.text.toLong() }
    val dash by literalToken("-")
    val sep by literalToken(",")
    val range by long * -dash and long map { (fst, snd) -> fst..snd }
    override val root: Parser<List<LongRange>> by zeroOrMore(range * -optional(sep))
}

fun main() = aoc(2025, 2, { grammar.parse(it).getOrElse { e -> error(e) } }) {
    part1 { ranges ->
        ranges
            .sumOf { range ->
                range.asSequence().filter { id ->
                    val numDigits = id.numDigits()
                    if (numDigits % 2 != 0) {
                        false
                    } else {
                        val half = pow10(numDigits / 2)
                        (id / half) == (id % half)
                    }
                }.sum()
            }
    }

    part2 { ranges ->
        fun isInvalid(id: Long): Boolean {
            val numDigits = id.numDigits()
            return numDigits.factors().filterNot { it == numDigits }.any { groupSize ->
                id.groupings(groupSize).allEqual()
            }
        }

        ranges
            .asSequence()
            .flatMap { it.asSequence() }
            .filter { isInvalid(it) }
            .sum()
    }
}
