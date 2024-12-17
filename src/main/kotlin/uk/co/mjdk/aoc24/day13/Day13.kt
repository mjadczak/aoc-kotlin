package uk.co.mjdk.aoc24.day13

import uk.co.mjdk.aoc.aoc

data class XyCoord(val x: Int, val y: Int)
data class AbCoord(val a: Int, val b: Int) {
    val score: Int get() = 3 * a + b
}

data class Machine(val aButton: XyCoord, val bButton: XyCoord, val prize: XyCoord) {
    companion object {
        private val pat =
            Regex("""Button A: X\+(\d+), Y\+(\d+)\nButton B: X\+(\d+), Y\+(\d+)\nPrize: X=(\d+), Y=(\d+)""")

        fun parse(input: String): Machine {
            pat.matchAt(input, 0)?.let { m ->
                val (ax, ay, bx, by, px, py) = m.destructured
                return Machine(
                    XyCoord(ax.toInt(), ay.toInt()), XyCoord(bx.toInt(), by.toInt()), XyCoord(px.toInt(), py.toInt())
                )
            } ?: throw IllegalArgumentException("Invalid machine input $input")
        }
    }
}

fun main() = aoc(2024, 13, { input -> input.split("\n\n").map(Machine::parse) }) {
    part1 { machines ->
        machines.sumOf { machine ->
            // basis change
            val (a, c) = machine.aButton
            val (b, d) = machine.bButton
            val p = machine.prize
            val invDet = a * d - b * c
            val aRes = d * p.x - b * p.y
            val bRes = -c * p.x + a * p.y
            if (aRes % invDet != 0 || bRes % invDet != 0) {
                // no integer solution
                0
            } else {
                AbCoord(aRes / invDet, bRes / invDet).score
            }
        }
    }
}
