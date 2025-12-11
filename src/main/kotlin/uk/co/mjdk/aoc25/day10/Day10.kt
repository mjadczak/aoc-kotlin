package uk.co.mjdk.aoc25.day10

import me.alllex.parsus.parser.*
import me.alllex.parsus.token.*
import org.ojalgo.optimisation.ExpressionsBasedModel
import org.ojalgo.optimisation.Optimisation
import org.ojalgo.optimisation.Variable
import uk.co.mjdk.aoc.aoc
import uk.co.mjdk.aoc.format
import java.util.BitSet
import kotlin.math.roundToInt

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

    fun toggled(button: BitButton): Lights = Lights(bitSet.copy().also { it.xor(button.bitSet) })

    companion object {
        val Empty = Lights(BitSet())
    }
}

@JvmInline
value class BitButton(val bitSet: BitSet) {
    constructor(indexes: List<Int>) : this(BitSet(indexes.max()).also { indexes.forEach { i -> it.set(i) } })
}

// need custom equals so can't be a value class
data class Vector(val ary: IntArray) {
    constructor(items: Collection<Int>) : this(items.toIntArray())

    val size get() = ary.size

    val isZero get() = ary.all { it == 0 }
    val isNonNegative get() = ary.all { it >= 0 }

    operator fun get(i: Int) = ary[i]

    operator fun plus(other: Vector): Vector {
        require(other.size == size)
        return Vector(IntArray(size) { ary[it] + other.ary[it] })
    }

    operator fun minus(other: Vector): Vector {
        require(other.size == size)
        return Vector(IntArray(size) { ary[it] - other.ary[it] })
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vector) return false
        return ary.contentEquals(other.ary)
    }

    override fun hashCode(): Int = ary.contentHashCode()

    companion object {
        fun fromIndices(size: Int, setIndices: List<Int>): Vector =
            Vector(IntArray(size).also { setIndices.forEach { i -> it[i] = 1 } })
    }
}

data class Machine(val desiredLights: Lights, val buttons: List<List<Int>>, val desiredJoltage: List<Int>) {
    constructor(
        desiredLights: List<Boolean>,
        buttons: List<List<Int>>,
        joltage: List<Int>
    ) : this(Lights(desiredLights), buttons, joltage)

    val bitButtons = buttons.map { BitButton(it) }
    val joltageVector = Vector(desiredJoltage)
    val buttonVectors = buttons.map { Vector.fromIndices(joltageVector.size, it) }
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
                machine.bitButtons.forEach { button ->
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

    part2 { machines ->
        machines.mapIndexed { machineIdx, machine ->
            val model = ExpressionsBasedModel()
            val maxCoeff = machine.desiredJoltage.sum()

            val vars = machine.buttonVectors.mapIndexed { bidx, button ->
                model.newVariable("c_$bidx").integer().lower(0).upper(maxCoeff.toLong()).weight(1) to button
            }

            machine.desiredJoltage.forEachIndexed { resIdx, target ->
                val expr = model.addExpression("pos_$resIdx")
                    .level(target.toLong())
                vars.forEach { (cVar, vector) ->
                    if (vector[resIdx] == 1) {
                        expr.set(cVar, 1)
                    }
                }
            }

            val result = model.minimise()
            when (result.state) {
                Optimisation.State.OPTIMAL, Optimisation.State.DISTINCT -> {
                    result.value.roundToInt()
                }

                else -> {
                    error("Could not find solution: $result")
                }
            }
        }.sum()
    }
}
