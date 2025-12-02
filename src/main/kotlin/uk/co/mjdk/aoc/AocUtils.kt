package uk.co.mjdk.aoc


fun Int.format(digits: Int) = "%0${digits}d".format(this)

fun <T> Sequence<T>.splitBy(shouldSplit: (T) -> Boolean): Sequence<List<T>> {
    return sequence {
        val buffer = mutableListOf<T>()
        for (current in this@splitBy) {
            if (shouldSplit(current)) {
                yield(buffer.toList())
                buffer.clear()
            } else {
                buffer.add(current)
            }
        }
        if (buffer.isNotEmpty()) {
            yield(buffer)
        }
    }
}

fun <T> Collection<T>.repeatForever(): Sequence<T> = sequence {
    while(true) {
        yieldAll(this@repeatForever)
    }
}

infix fun Int.arithMod(other: Int): Int {
    val res = this % other
    return if (res < 0) {
        res + other
    } else {
        res
    }
}
