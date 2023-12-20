package uk.co.mjdk.aoc23.day19

import uk.co.mjdk.aoc.aoc

private enum class Field {
    X,
    M,
    A,
    S
}

private enum class Op {
    Lt,
    Gt
}

private data class Item(val fields: Map<Field, Int>) {
    val rating: Int
        get() = fields.values.sum()
}

private data class Condition(val field: Field, val op: Op, val value: Int) {
    fun matches(item: Item): Boolean = when (op) {
        Op.Lt -> item.fields[field]!! < value
        Op.Gt -> item.fields[field]!! > value
    }
}

private data class Rule(val condition: Condition?, val target: String) {
    fun process(item: Item): String? = target.takeUnless { condition?.matches(item) == false }
}

private data class Workflow(val id: String, val rules: List<Rule>) {
    fun process(item: Item): String = rules.firstNotNullOf { it.process(item) }
}

private fun parse(input: String): Pair<List<Workflow>, List<Item>> {
    val (workflowsStr, itemsStr) = input.split("\n\n")
    val workflows = workflowsStr.lines().map { line ->
        val (id, restStr) = line.split('{')
        val rules = restStr.dropLast(1).split(',').map { ruleStr ->
            val parts = ruleStr.split(':')
            when (parts.size) {
                1 -> Rule(null, parts.first())
                2 -> {
                    val (condStr, id) = parts
                    val field = Field.valueOf(condStr.take(1).uppercase())
                    val cond = when (condStr[1]) {
                        '<' -> Op.Lt
                        '>' -> Op.Gt
                        else -> throw IllegalArgumentException()
                    }
                    val value = condStr.drop(2).toInt()
                    Rule(Condition(field, cond, value), id)
                }

                else -> throw IllegalArgumentException(ruleStr)
            }
        }
        Workflow(id, rules)
    }
    val items = itemsStr.lines().map { line ->
        val flds = mutableMapOf<Field, Int>()
        line.drop(1).dropLast(1).split(',').forEach { str ->
            val (fld, qty) = str.split('=')
            val field = Field.valueOf(fld.uppercase())
            flds[field] = qty.toInt()
        }
        Item(flds)
    }
    return workflows to items
}

fun main() = aoc(2023, 19, ::parse) {
    part1 { (workflowList, items) ->
        val workflows = workflowList.associateBy { it.id }

        items.filter { item ->
            generateSequence("in") { workflowId ->
                workflows[workflowId]!!.process(item)
            }.first { it == "A" || it == "R" } == "A"
        }.sumOf { it.rating }
    }
}
