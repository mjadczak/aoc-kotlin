package uk.co.mjdk.aoc23.day07

import uk.co.mjdk.aoc.aoc

@Suppress("EnumEntryName")
private enum class Card {
    Joker,
    _2,
    _3,
    _4,
    _5,
    _6,
    _7,
    _8,
    _9,
    T,
    J,
    Q,
    K,
    A;

    fun jokered(): Card = if (this == J) Joker else this

    companion object {
        fun parse(input: Char): Card = if (input.isDigit()) Card.valueOf("_$input") else Card.valueOf(input.toString())
    }
}

private enum class HandType {
    HighCard,
    OnePair,
    TwoPair,
    ThreeOfAKind,
    FullHouse,
    FourOfAKind,
    FiveOfAKind
}

private data class Hand(val cards: List<Card>) {
    init {
        require(cards.size == 5)
    }

    val handType = computeHandType(cards)

    companion object {
        private fun computeHandType(cards: List<Card>): HandType {
            val groups = cards.groupingBy { it }.eachCount()
            val jokers = groups[Card.Joker] ?: 0
            val counts = groups.filterNot { it.key == Card.Joker }.values.sortedDescending().toMutableList()
            // I think that the best place to put the jokers is always in the already-highest group
            if (counts.isEmpty()) {
                counts.add(jokers)
            } else {
                counts[0] += jokers
            }

            return when (counts) {
                listOf(5) -> HandType.FiveOfAKind
                listOf(4, 1) -> HandType.FourOfAKind
                listOf(3, 2) -> HandType.FullHouse
                listOf(3, 1, 1) -> HandType.ThreeOfAKind
                listOf(2, 2, 1) -> HandType.TwoPair
                listOf(2, 1, 1, 1) -> HandType.OnePair
                listOf(1, 1, 1, 1, 1) -> HandType.HighCard
                else -> throw IllegalStateException()
            }
        }
    }
}

private fun <T : Comparable<T>> listComparator(): Comparator<List<T>> = Comparator { l, r ->
    require(l.size == r.size)
    l.zip(r).forEach { (li, ri) ->
        val comp = li.compareTo(ri)
        if (comp != 0) return@Comparator comp
    }
    0
}

// if a < b then b beats a
private val ScoringComparator: Comparator<Hand> =
    compareBy<Hand> { it.handType }.thenComparing({ it.cards }, listComparator())

private data class Game(val hand: Hand, val bid: Int) {
    fun jokered(): Game = copy(hand = hand.copy(cards = hand.cards.map { it.jokered() }))
}

private fun parse(input: String): List<Game> = input.lines().map { line ->
    val (h, b) = line.split(" ")
    Game(Hand(h.map { Card.parse(it) }), b.toInt())
}

fun main() = aoc(2023, 7, ::parse) {
    fun List<Game>.getWinnings(): Int = sortedWith(compareBy(ScoringComparator) { it.hand }).withIndex().sumOf {
        (it.index + 1) * it.value.bid
    }

    part1 { games ->
        games.getWinnings()
    }

    part2 { games ->
        games.map { it.jokered() }.getWinnings()
    }
}
