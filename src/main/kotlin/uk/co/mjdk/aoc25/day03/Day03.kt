package uk.co.mjdk.aoc25.day03

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import uk.co.mjdk.aoc.aoc

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

fun main() = aoc(2025, 3, object : Grammar<List<List<Int>>>() {
    val digit by regexToken("\\d").map { it.text.toInt() }
    val nl by literalToken("\n")
    val bank = oneOrMore(digit)
    override val root by oneOrMore(bank and -optional(-nl))
}) {
    example(
        """
        987654321111111
        811111111111119
        234234234234278
        818181911112111
    """.trimIndent()
    )

    part1 { banks ->
        banks.sumOf { bank ->
            val digitIndexes = bank.asSequence().withIndex().groupBy { it.value }
            val greedyDigits = sequence {
                for (digit in 9 downTo 1) {
                    digitIndexes[digit]?.let { yieldAll(it) }
                }
            }
            val first = greedyDigits.first { it.index != bank.indices.last }
            val second = greedyDigits.first { it.index > first.index }
            first.value * 10 + second.value
        }
    }

    part2 { banks ->
        banks.sumOf { bank ->
            val maxNumDigits = 12
            val totalNumDigits = bank.size
            check(maxNumDigits <= totalNumDigits)
            // biggest number of [n] digits, which can be formed using numbers from indices [m]..<s
            val memo = List(maxNumDigits + 1) {
                MutableList(totalNumDigits) { null as Long? }
            }
            for (numDigits in 1..maxNumDigits) {
                var maximalDigit = 0
                var maximalIndex = -1
                for (leftmostIdx in (totalNumDigits - numDigits) downTo maxNumDigits - numDigits) {
                    val digit = bank[leftmostIdx]
                    if (digit >= maximalDigit) {
                        maximalDigit = digit
                        maximalIndex = leftmostIdx
                    }
                    val maxRestValue = if (numDigits == 1) 0L else memo[numDigits - 1][maximalIndex + 1]!!
                    val total = maximalDigit * pow10(numDigits - 1) + maxRestValue
                    memo[numDigits][leftmostIdx] = total
                }
            }
            memo[maxNumDigits][0]!!
        }
    }
}
