package uk.co.mjdk.aoc25.day06

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

data class Input1(
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
        fun parse(input: String): Input1 {
            val lines = input.trim().lines()
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
            return Input1(operandRows, operatorRow)
        }
    }
}

fun parse2(input: String): Sequence<Problem> = sequence {
    val lines = input.lineSequence().filterNot(String::isEmpty).toList()
    val operandLines = lines.dropLast(1)
    val operators = lines.last().trim().split("\\s+".toRegex()).map {
        when (it) {
            "+" -> Operator.Add
            "*" -> Operator.Multiply
            else -> error(it)
        }
    }

    var charIdx = 0
    operators.forEach { operator ->
        val numbers = buildList {
            while (charIdx < operandLines.first().length) {
                val operand = operandLines
                    .map { it[charIdx] }
                    .filter { it.isDigit() }
                    .let {
                        if (it.isEmpty()) null else it.joinToString("").toLong()
                    }
                charIdx++
                if (operand == null) break
                add(operand)
            }
        }
        check(numbers.isNotEmpty()) { "Failed at idx = $charIdx" }
        yield(Problem(numbers, operator))
    }
}

fun main() = aoc(2025, 6, trimString = false) {
    part1 { str ->
        Input1.parse(str).problems.sumOf { it.calculate() }
    }

    part2 { str ->
        parse2(str).sumOf { it.calculate() }
    }
}
