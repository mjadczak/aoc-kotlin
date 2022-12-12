package uk.co.mjdk.aoc22.day10

import uk.co.mjdk.aoc.aocInput

sealed interface Instruction {
    object Noop : Instruction
    data class Addx(val operand: Int) : Instruction

    companion object {
        fun parse(line: String): Instruction {
            if (line == "noop") {
                return Noop
            }
            val (addx, sOp) = line.split(' ')
            assert(addx == "addx")
            return Addx(sOp.toInt())
        }
    }
}

data class CpuState(val cycleNum: Int, val xReg: Int)

fun calculateStates(instructions: Sequence<Instruction>): Sequence<CpuState> = sequence {
    var cycle = 0
    var xReg = 1
    for (instruction in instructions) {
        when (instruction) {
            is Instruction.Noop -> {
                cycle += 1
                yield(CpuState(cycle, xReg))
            }

            is Instruction.Addx -> {
                cycle += 1
                yield(CpuState(cycle, xReg))
                cycle += 1
                yield(CpuState(cycle, xReg))
                xReg += instruction.operand
            }
        }
    }
}

fun part1() {
    aocInput(22, 10).useLines { lines ->
        val states = calculateStates(lines.map(Instruction::parse))

        val statesOfInterest = states.filter { (it.cycleNum + 20) % 40 == 0 }.toList()
        assert(statesOfInterest.last().cycleNum == 220)
        println(statesOfInterest.sumOf { it.cycleNum * it.xReg })
    }
}

fun part2() {
    aocInput(22, 10).useLines { lines ->
        val states = calculateStates(lines.map(Instruction::parse))

        val pixels = states.map {
            (-1..1).contains((it.cycleNum - 1) % 40 - it.xReg)
        }

        pixels.chunked(40).forEach { row ->
            println(row.map { if (it) '#' else '.' }.joinToString(""))
        }
    }
}

fun main() {
    part1()
    part2()
}
