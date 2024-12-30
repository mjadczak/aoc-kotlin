package uk.co.mjdk.aoc24.day17

import org.apache.commons.math3.util.ArithmeticUtils.pow
import uk.co.mjdk.aoc.aoc

enum class Instruction(val opcode: Int) {
    adv(0),
    bxl(1),
    bst(2),
    jnz(3),
    bxc(4),
    `out`(5),
    bdv(6),
    cdv(7);

    companion object {
        operator fun invoke(opcode: Int): Instruction =
            entries.find { it.opcode == opcode } ?: throw IllegalStateException("Unknown opcode $opcode")
    }
}

class Machine(
    private var regA: Int = 0,
    private var regB: Int = 0,
    private var regC: Int = 0,
    val program: List<Int>
) {
    private var ip: Int = 0
    private val _output = mutableListOf<Int>()
    val output get() = _output.toList()

    override fun toString(): String {
        return "A:$regA B:$regB C:$regC IP:$ip / ${output.joinToString(",")}"
    }

    private fun isDone(): Boolean = ip >= program.size

    private fun readValue(): Int {
        check(!isDone())
        val res = program[ip]
        ip++
        check(res >= 0 && res <= 7)
        return res
    }

    private fun readInst(): Instruction = Instruction(readValue())
    private fun readLiteral(): Int = readValue()
    private fun readCombo(): Int = when (val value = readValue()) {
        in 0..3 -> value
        4 -> regA
        5 -> regB
        6 -> regC
        else -> throw IllegalStateException("Invalid combo operand $value")
    }

    @Suppress("DEPRECATION")
    private fun processInstruction(instruction: Instruction) {
        check(ip % 2 == 1)
        when (instruction) {
            Instruction.adv -> {
                val numerator = regA
                val denominator = pow(2, readCombo())
                regA = numerator / denominator
            }

            Instruction.bxl -> {
                regB = regB xor readLiteral()
            }

            Instruction.bst -> {
                regB = readCombo() % 8
            }

            Instruction.jnz -> {
                val target = readLiteral()
                if (regA != 0) {
                    ip = target.toInt()
                }
            }

            Instruction.bxc -> {
                readLiteral()
                regB = regB xor regC
            }

            Instruction.out -> {
                _output.add(readCombo() % 8)
            }

            Instruction.bdv -> {
                val numerator = regA
                val denominator = pow(2, readCombo())
                regB = numerator / denominator
            }

            Instruction.cdv -> {
                val numerator = regA
                val denominator = pow(2, readCombo())
                regC = numerator / denominator
            }
        }
    }

    fun processAll() {
        while (!isDone()) {
            processInstruction(readInst())
        }
    }

    companion object {
        fun parse(input: String): Machine {
            val pat = Regex("""Register A: (\d+)\nRegister B: (\d+)\nRegister C: (\d+)\n\nProgram: (\d(?:,\d)*)""")
            return pat.matchEntire(input)?.let {
                val (a, b, c, prog) = it.destructured
                Machine(a.toInt(), b.toInt(), c.toInt(), prog.split(',').map { it.toInt() })
            } ?: throw IllegalArgumentException("Invalid input")
        }
    }
}

fun main() = aoc(2024, 17, Machine::parse) {
    part1 { machine ->
        machine.processAll()
        machine.output.joinToString(",")
    }
}
