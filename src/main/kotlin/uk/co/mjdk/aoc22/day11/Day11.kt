package uk.co.mjdk.aoc22.day11

import uk.co.mjdk.aoc.aocInput

sealed interface Operation {
    fun apply(worry: Long): Long

    object Square : Operation {
        override fun apply(worry: Long): Long = worry * worry
    }

    data class Multiply(val operand: Long) : Operation {
        override fun apply(worry: Long): Long = worry * operand
    }

    data class Add(val operand: Long) : Operation {
        override fun apply(worry: Long): Long = worry + operand
    }
}

sealed interface WorryManagement {
    fun apply(worry: Long): Long

    object DivideByThree : WorryManagement {
        override fun apply(worry: Long): Long = worry / 3
    }

    data class Mod(val modulo: Long) : WorryManagement {
        override fun apply(worry: Long): Long = worry % modulo
    }
}

enum class WorryManagementStrategy {
    DivideByThree,
    Modulo
}

data class MonkeySpec(
    val operation: Operation,
    val divisibleTest: Long,
    val trueTarget: Int,
    val falseTarget: Int
)

data class Monkey(
    val index: Int,
    val spec: MonkeySpec,
    val items: MutableList<Long>,
    var inspectionCounter: Long = 0
) {
    fun inspectItems(strategy: WorryManagement): Sequence<Pair<Int, Long>> = sequence {
        items.forEach { item ->
            var worry = item
            inspectionCounter += 1
            worry = spec.operation.apply(worry)
            worry = strategy.apply(worry)
            val idx = if (worry % spec.divisibleTest == 0L) spec.trueTarget else spec.falseTarget
            yield(idx to worry)
        }
        items.clear()
    }
}

val monkeyPat = Regex(
    """
    Monkey (\d+):
      Starting items: ([\d, ]+)
      Operation: new = old ([+*]) (old|\d+)
      Test: divisible by (\d+)
        If true: throw to monkey (\d+)
        If false: throw to monkey (\d+)
""".trimIndent()
)

fun getMonkeys(): List<Monkey> {
    val inputStr = aocInput(22, 11).use { it.readText() }
    return monkeyPat.findAll(inputStr).withIndex().map { iv ->
        val strIdx = iv.value.groupValues[1]
        val strItems = iv.value.groupValues[2]
        val strOperation = iv.value.groupValues[3]
        val strOperand = iv.value.groupValues[4]
        val strDivisible = iv.value.groupValues[5]
        val strTrueIdx = iv.value.groupValues[6]
        val strFalseIdx = iv.value.groupValues[7]

        val operation = if (strOperand == "old") {
            assert(strOperation == "*")
            Operation.Square
        } else {
            when (strOperation) {
                "*" -> {
                    Operation.Multiply(strOperand.toLong())
                }

                "+" -> {
                    Operation.Add(strOperand.toLong())
                }

                else -> {
                    throw IllegalArgumentException("Unrecognised operation $strOperation")
                }
            }
        }

        val spec = MonkeySpec(
            operation,
            strDivisible.toLong(),
            strTrueIdx.toInt(),
            strFalseIdx.toInt()
        )

        val items = strItems.split(", ").map { it.toLong() }.toMutableList()
        val index = strIdx.toInt()
        assert(iv.index == index)

        Monkey(index, spec, items)
    }.toList()
}

fun run(rounds: Int, strategy: WorryManagementStrategy) {
    val monkeys = getMonkeys()
    val worryManagement = when (strategy) {
        WorryManagementStrategy.DivideByThree -> WorryManagement.DivideByThree
        WorryManagementStrategy.Modulo -> WorryManagement.Mod(monkeys.map { it.spec.divisibleTest }.reduce(Long::times))
    }
    repeat(rounds) {
        monkeys.forEach { monkey ->
            monkey.inspectItems(worryManagement).forEach { (targetIdx, item) ->
                assert(targetIdx != monkey.index)
                monkeys[targetIdx].items.add(item)
            }
        }
    }

    val monkeyBusiness =
        monkeys.map { it.inspectionCounter }.sortedDescending().asSequence().take(2).reduce(Long::times)
    println(monkeyBusiness)
}

fun main() {
    run(20, WorryManagementStrategy.DivideByThree)
    run(10000, WorryManagementStrategy.Modulo)
}
