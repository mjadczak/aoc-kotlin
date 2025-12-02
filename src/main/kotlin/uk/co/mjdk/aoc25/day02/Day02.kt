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
}
