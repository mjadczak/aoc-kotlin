package uk.co.mjdk.aoc24.day03

import uk.co.mjdk.aoc.aoc

val mulpat = Regex("""mul\((\d+),(\d+)\)""")
val allpat = Regex("""mul\((\d+),(\d+)\)|do\(\)|don't\(\)""")

fun main() = aoc(2024, 3) {
    part1 { input ->
        mulpat.findAll(input).sumOf { mr ->
            val (x, y) = mr.destructured
            x.toInt() * y.toInt()
        }
    }

    data class State(val sum: Int, val enabled: Boolean)
    part2 { input ->
        allpat.findAll(input).fold(State(0, true)) { st, mr ->
            when {
                mr.value.startsWith("mul") && st.enabled -> {
                    val (x, y) = mr.destructured
                    st.copy(sum = st.sum + x.toInt() * y.toInt())
                }

                mr.value == "do()" -> st.copy(enabled = true)
                mr.value == "don't()" -> st.copy(enabled = false)
                else -> st
            }
        }.sum
    }
}
