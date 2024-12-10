package uk.co.mjdk.aoc24.day07

import uk.co.mjdk.aoc.aoc
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

fun Long.concat(other: Long): Long {
    val numDigits = floor(log10(other.toDouble()) + 1.0)
    return this * (10.0.pow(numDigits).toLong()) + other
}

data class Equation(val result: Long, val inputs: List<Long>) {
    fun isValid(allowConcat: Boolean): Boolean {
        fun check(total: Long, remaining: List<Long>): Boolean {
            if (total > result) return false
            if (remaining.isEmpty()) return total == result
            val next = remaining.first()
            val remaining2 = remaining.subList(1, remaining.size)
            return check(total + next, remaining2) || check(
                total * next,
                remaining2
            ) || (allowConcat && check(total.concat(next), remaining2))
        }

        return check(inputs.first(), inputs.subList(1, inputs.size))
    }

    companion object {
        fun parse(input: String): Equation {
            val (resStr, inputsStr) = input.split(": ")
            val inputs = inputsStr.split(" ").map { it.toLong() }
            return Equation(resStr.toLong(), inputs)
        }
    }
}

fun main() = aoc(2024, 7, { it.lines().map(Equation::parse) }) {
    part1 { eqns ->
        eqns.filter { it.isValid(false) }.sumOf { it.result }
    }

    part2 { eqns ->
        eqns.filter { it.isValid(true) }.sumOf { it.result }
    }
}
