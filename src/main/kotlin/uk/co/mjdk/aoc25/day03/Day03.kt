package uk.co.mjdk.aoc25.day03

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.literalToken
import me.alllex.parsus.token.regexToken
import uk.co.mjdk.aoc.aoc

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
}
