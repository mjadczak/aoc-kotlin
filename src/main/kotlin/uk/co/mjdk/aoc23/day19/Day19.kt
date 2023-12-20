package uk.co.mjdk.aoc23.day19

import uk.co.mjdk.aoc.aoc
import java.util.ArrayDeque

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

    part2 { (workflowList, _) ->
        val workflows = workflowList.associateBy { it.id }

        data class MultiItem(val fields: Map<Field, IntRange>) {
            val combinations: Long
                get() = fields.values.map { it.count().toLong() }.reduce(Long::times)
        }

        val initial = MultiItem(
            mapOf(
                Field.X to 1..4000,
                Field.M to 1..4000,
                Field.A to 1..4000,
                Field.S to 1..4000,
            )
        )

        val queue = ArrayDeque<Pair<String, MultiItem>>()
        queue.offer("in" to initial)
        var total = 0L

        // left: reject, right: accept
        fun IntRange.split(op: Op, value: Int): Pair<IntRange, IntRange> = when (op) {
            Op.Lt -> value..endInclusive to start..<value
            Op.Gt -> start..value to (value + 1)..endInclusive
        }

        while (queue.isNotEmpty()) {
            val (id, item) = queue.poll()
            if (id == "A") {
                total += item.combinations
                continue
            }
            if (id == "R") {
                continue
            }

            val workflow = workflows[id]!!
            var remaining = item
            for (rule in workflow.rules) {
                if (remaining.combinations == 0L) break
                val c = rule.condition
                if (c == null) {
                    queue.offer(rule.target to remaining)
                    break
                }
                val (rejected, accepted) = remaining.fields[c.field]!!.split(c.op, c.value)
                queue.offer(rule.target to MultiItem(remaining.fields + (c.field to accepted)))
                remaining = MultiItem(remaining.fields + (c.field to rejected))
            }
        }

        total
    }
}
