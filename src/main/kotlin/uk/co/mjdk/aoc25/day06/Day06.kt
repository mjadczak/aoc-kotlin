package uk.co.mjdk.aoc25.day06

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.*
import uk.co.mjdk.aoc.aoc

enum class Operator {
    Add,
    Multiply,
}

data class Problem(
    val operands: List<Long>,
    val operator: Operator,
) {
    fun calculate(): Long = when (operator) {
        Operator.Add -> operands.sum()
        Operator.Multiply -> operands.reduce { a, b -> a * b }
    }
}

data class Input(
    val operandRows: List<List<Long>>,
    val operatorRow: List<Operator>,
) {
    init {
        require((operandRows.map { it.size } + operatorRow.size).distinct().size == 1)
    }

    val problems: Sequence<Problem> = operatorRow.indices.asSequence().map { idx ->
        Problem(operandRows.map { it[idx] }, operatorRow[idx])
    }

    companion object {
        fun parse(input: String): Input {
            val lines = input.lines()
            val operandRows = lines.dropLast(1).map { line ->
                line.trim().split("\\s+".toRegex()).map(String::toLong)
            }
            val operatorRow = lines.last().trim().split("\\s+".toRegex()).map {
                when (it) {
                    "+" -> Operator.Add
                    "*" -> Operator.Multiply
                    else -> error(it)
                }
            }
            return Input(operandRows, operatorRow)
        }
    }
}

fun main() = aoc(2025, 6, Input::parse) {
    part1 { input ->
        input.problems.sumOf { it.calculate() }
    }
}
