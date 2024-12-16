package uk.co.mjdk.aoc24.day11

import uk.co.mjdk.aoc.aoc

// is this even faster than just iterating?
fun Long.numDigits(): Int {
    var res = 0
    var num = this
    while (num != 0L) {
        num /= 10
        res += 1
    }
    return res
}

fun pow10(power: Int): Long {
    check(power >= 0)
    var res = 1L
    var pow = power
    while (pow > 0) {
        res *= 10
        pow -= 1
    }
    return res
}

fun List<Long>.blink(): List<Long> = buildList {
    this@blink.forEach { stone ->
        if (stone == 0L) {
            add(1L)
        } else {
            val numDigits = stone.numDigits()
            if (numDigits % 2 == 0) {
                val pow = pow10(numDigits / 2)
                val first = stone / pow
                val second = stone - (first * pow)
                add(first)
                add(second)
            } else {
                add(stone * 2024L)
            }
        }
    }
}

data class Key(val stone: Long, val blinks: Int)

val cache = mutableMapOf<Key, Long>()
fun totalNum(key: Key): Long = if (key.blinks == 0) 1L
else listOf(key.stone).blink().sumOf { st -> getTotalNum(Key(st, key.blinks - 1)) }


fun getTotalNum(key: Key): Long = cache[key] ?: totalNum(key).also { cache[key] = it }

fun main() = aoc(2024, 11, { input -> input.split(" ").map { it.toLong() } }) {
    example("125 17")

    part1 { stones ->
        var current = stones
        repeat(25) {
            current = current.blink()
        }
        current.size
    }

    part2 { stones ->
        stones.map { st -> Key(st, 75) }.sumOf(::getTotalNum)
    }
}
