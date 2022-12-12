package uk.co.mjdk.aoc22.day11

import uk.co.mjdk.aoc.aocInput

enum class Operation {
    Multiply,
    Add,
    Square
}

data class MonkeySpec(
    val operation: Operation,
    val operand: Int,
    val divisibleTest: Int,
    val trueTarget: Int,
    val falseTarget: Int
)

data class Monkey(
    val index: Int,
    val spec: MonkeySpec,
    val items: MutableList<Int>,
    var inspectionCounter: Int = 0
) {
    fun inspectItems(): Sequence<Pair<Int, Int>> = sequence {
        items.forEach { item ->
            var worry = item
            inspectionCounter += 1
            when (spec.operation) {
                Operation.Square -> worry *= worry
                Operation.Add -> worry += spec.operand
                Operation.Multiply -> worry *= spec.operand
            }
            worry /= 3
            val idx = if (worry % spec.divisibleTest == 0) spec.trueTarget else spec.falseTarget
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

        val (operation, operand) = if (strOperand == "old") {
            assert(strOperation == "*")
            Operation.Square to -1
        } else {
            val operation = if (strOperation == "*") {
                Operation.Multiply
            } else if (strOperation == "+") {
                Operation.Add
            } else {
                throw IllegalArgumentException("Unrecognised operation $strOperation")
            }
            operation to strOperand.toInt()
        }

        val spec = MonkeySpec(
            operation,
            operand,
            strDivisible.toInt(),
            strTrueIdx.toInt(),
            strFalseIdx.toInt()
        )

        val items = strItems.split(", ").map { it.toInt() }.toMutableList()
        val index = strIdx.toInt()
        assert(iv.index == index)

        Monkey(index, spec, items)
    }.toList()
}

fun main() {
    val monkeys = getMonkeys()
    repeat(20) {
        monkeys.forEach { monkey ->
            monkey.inspectItems().forEach { (targetIdx, item) ->
                assert(targetIdx != monkey.index)
                monkeys[targetIdx].items.add(item)
            }
        }
    }

    val monkeyBusiness = monkeys.map { it.inspectionCounter }.sortedDescending().asSequence().take(2).reduce(Int::times)
    println(monkeyBusiness)
}
