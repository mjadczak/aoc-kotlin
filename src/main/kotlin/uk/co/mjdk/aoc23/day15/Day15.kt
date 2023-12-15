package uk.co.mjdk.aoc23.day15

import uk.co.mjdk.aoc.aoc

private fun String.hash(): Int = fold(0) { acc, char ->
    @Suppress("ComplexRedundantLet")
    acc
        .let { it + char.code }
        .let { it * 17 }
        .let { it % 256 }
}

private class BoxMap {
    private val buckets: List<ArrayDeque<Pair<String, Int>>> = List(256) { ArrayDeque() }

    fun addLens(label: String, focalLength: Int) {
        val bucket = buckets[label.hash()]
        val existingIdx = bucket.indexOfFirst { it.first == label }.takeUnless { it == -1 }

        if (existingIdx != null) {
            bucket[existingIdx] = bucket[existingIdx].copy(second = focalLength)
        } else {
            bucket.addLast(label to focalLength)
        }
    }

    fun removeLens(label: String) {
        val bucket = buckets[label.hash()]
        bucket.indexOfFirst { it.first == label }.takeUnless { it == -1 }?.let { idx ->
            bucket.removeAt(idx)
        }
    }

    fun focusingPower(): Int = buckets.withIndex().sumOf { bucket ->
        bucket.value.withIndex().sumOf { lens ->
            (bucket.index + 1) * (lens.index + 1) * lens.value.second
        }
    }

    fun pretty() = buildString {
        buckets.withIndex().forEach { bucket ->
            append("Box ${bucket.index}:")
            bucket.value.withIndex().forEach { lens ->
                append(" [${lens.value.first} ${lens.value.second}]")
            }
            appendLine()
        }
    }
}

fun main() = aoc(2023, 15) {
    part1 { input ->
        input.splitToSequence(',').sumOf { it.hash() }
    }

    part2 { input ->
        val boxes = BoxMap()
        for (str in input.splitToSequence(',')) {
            if (str.last() == '-') {
                boxes.removeLens(str.dropLast(1))
            } else if (str.last().isDigit()) {
                boxes.addLens(str.dropLast(2), str.last().digitToInt())
            } else {
                throw IllegalStateException()
            }
        }
        boxes.focusingPower()
    }
}
