package uk.co.mjdk.aoc25.day10

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.*
import uk.co.mjdk.aoc.aoc
import java.util.BitSet

fun BitSet.copy(): BitSet = clone() as BitSet

@JvmInline
value class Lights(val bitSet: BitSet) {
    constructor(lights: List<Boolean>) : this(BitSet(lights.size).also {
        lights.forEachIndexed { i, b ->
            it.set(
                i,
                b
            )
        }
    })

    fun toggled(button: Button): Lights = Lights(bitSet.copy().also { it.xor(button.bitSet) })

    companion object {
        val Empty = Lights(BitSet())
    }
}

@JvmInline
value class Button(val bitSet: BitSet) {
    constructor(indexes: List<Int>) : this(BitSet(indexes.max()).also { indexes.forEach { i -> it.set(i) } })
}

data class Machine(val desiredLights: Lights, val buttons: List<Button>, val joltage: List<Int>) {
    constructor(
        desiredLights: List<Boolean>,
        buttons: List<List<Int>>,
        joltage: List<Int>
    ) : this(Lights(desiredLights), buttons.map { Button(it) }, joltage)
}

fun main() = aoc(2025, 10, object : Grammar<List<Machine>>() {
    val int by regexToken("\\d+").map { it.text.toInt() }
    val space by literalToken(" ")
    val bracketL by literalToken("[")
    val bracketR by literalToken("]")
    val parenL by literalToken("(")
    val parenR by literalToken(")")
    val braceL by literalToken("{")
    val braceR by literalToken("}")
    val comma by literalToken(",")
    val lightOn by literalToken("#").map { true }
    val lightOff by literalToken(".").map { false }
    val nl by literalToken("\n")

    val lights by -bracketL and oneOrMore(lightOn or lightOff) and -bracketR
    val button by -parenL and oneOrMore(int and -optional(comma)) and -parenR
    val joltage by -braceL and oneOrMore(int and -optional(comma)) and -braceR

    val machine by lights and -space and oneOrMore(button and -space) and joltage and -nl map { (l, b, j) ->
        Machine(l, b, j)
    }

    override val root: Parser<List<Machine>> by oneOrMore(machine)
}, trimString = false) {
    part1 { machines ->
        machines.sumOf { machine ->
            data class State(val lights: Lights, val steps: Int)

            val visited = mutableSetOf<Lights>()
            val queue = ArrayDeque<State>()
            queue.addLast(State(Lights.Empty, 0))
            while (queue.isNotEmpty()) {
                val state = queue.removeFirst()
                if (state.lights == machine.desiredLights) {
                    return@sumOf state.steps
                }
                machine.buttons.forEach { button ->
                    val newLights = state.lights.toggled(button)
                    if (visited.add(newLights)) {
                        queue.addLast(
                            State(
                                newLights,
                                state.steps + 1,
                            )
                        )
                    }
                }
            }
            error("No end found for $machine!")
        }
    }
}
