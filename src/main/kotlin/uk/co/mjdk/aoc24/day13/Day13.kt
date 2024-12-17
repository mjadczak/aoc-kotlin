package uk.co.mjdk.aoc24.day13

import uk.co.mjdk.aoc.aoc

data class XyCoord(val x: Long, val y: Long)
data class AbCoord(val a: Long, val b: Long) {
    val score: Long get() = 3 * a + b
}

data class Machine(val aButton: XyCoord, val bButton: XyCoord, val prize: XyCoord) {
    val bigPrize = XyCoord(prize.x + 10000000000000L, prize.y + 10000000000000L)

    fun invert(xyCoord: XyCoord): AbCoord? {
        // basis change
        val (a, c) = aButton
        val (b, d) = bButton
        val p = xyCoord
        val invDet = a * d - b * c
        val aRes = d * p.x - b * p.y
        val bRes = -c * p.x + a * p.y
        return if (aRes % invDet != 0L || bRes % invDet != 0L) {
            // no integer solution
            null
        } else {
            AbCoord(aRes / invDet, bRes / invDet)
        }
    }

    companion object {
        private val pat =
            Regex("""Button A: X\+(\d+), Y\+(\d+)\nButton B: X\+(\d+), Y\+(\d+)\nPrize: X=(\d+), Y=(\d+)""")

        fun parse(input: String): Machine {
            pat.matchAt(input, 0)?.let { m ->
                val (ax, ay, bx, by, px, py) = m.destructured
                return Machine(
                    XyCoord(ax.toLong(), ay.toLong()),
                    XyCoord(bx.toLong(), by.toLong()),
                    XyCoord(px.toLong(), py.toLong())
                )
            } ?: throw IllegalArgumentException("Invalid machine input $input")
        }
    }
}

fun main() = aoc(2024, 13, { input -> input.split("\n\n").map(Machine::parse) }) {
    part1 { machines ->
        machines.sumOf { machine ->
            machine.invert(machine.prize)?.score ?: 0
        }
    }

    part2 { machines ->
        machines.sumOf { machine ->
            machine.invert(machine.bigPrize)?.score ?: 0
        }
    }
}
