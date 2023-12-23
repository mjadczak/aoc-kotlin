package uk.co.mjdk.aoc23.day22

import uk.co.mjdk.aoc.aoc
import uk.co.mjdk.aoc.format
import java.io.PrintStream
import java.util.*
import kotlin.math.min
import kotlin.math.max

@JvmInline
private value class Rgb(val value: Int) {
    val r: Int
        get() = ((value and 0xff0000) shr 16)
    val g: Int
        get() = ((value and 0xff00) shr 8)
    val b: Int
        get() = (value and 0xff)

    fun printed(stream: PrintStream = System.out, block: PrintStream.() -> Unit) {
        with(stream) {
            print("\u001b[38;2;")
            print(r)
            print(";")
            print(g)
            print(";")
            print(b)
            print("m")
            block()
            print("\u001B[0m")
        }
    }
}

private data class CoordXyz(val x: Int, val y: Int, val z: Int)

private data class Block(val lowerCorner: CoordXyz, val upperCorner: CoordXyz) {
    init {
        require(lowerCorner.z <= upperCorner.z)
    }

    val xEdge: IntRange
        get() = min(lowerCorner.x, upperCorner.x)..max(lowerCorner.x, upperCorner.x)

    val yEdge: IntRange
        get() = min(lowerCorner.y, upperCorner.y)..max(lowerCorner.y, upperCorner.y)

    val zEdge: IntRange
        get() = lowerCorner.z..upperCorner.z

    val bottom: Int
        get() = lowerCorner.z

    val top: Int
        get() = upperCorner.z

    // "exclusive height" - but it works for the way we use it
    val height: Int
        get() = top - bottom

    override fun toString(): String = buildString {
        append(lowerCorner.x)
        append(',')
        append(lowerCorner.y)
        append(',')
        append(lowerCorner.z)
        append('~')
        append(upperCorner.x)
        append(',')
        append(upperCorner.y)
        append(',')
        append(upperCorner.z)
    }

    val colour: Rgb
        get() = Rgb(
            Objects.hash(
                lowerCorner.x,
                lowerCorner.y,
                lowerCorner.z,
                upperCorner.x,
                upperCorner.y,
                upperCorner.z,
            )
        )

    companion object {
        fun parse(input: String): Block {
            val (l, r) = input.split('~').map { xyzStr ->
                val (x, y, z) = xyzStr.split(',').map { it.toInt() }
                CoordXyz(x, y, z)
            }
            return Block(l, r)
        }
    }
}

fun main() = aoc(2023, 22, { input -> input.lines().map { Block.parse(it) } }) {
    example(
        """
        1,0,1~1,2,1
        0,0,2~2,0,2
        0,2,3~2,2,3
        0,0,4~0,2,4
        2,0,5~2,2,5
        0,1,6~2,1,6
        1,1,8~1,1,9
    """.trimIndent()
    )

    part1 { blockList ->
        // start from the bottom and move to the top 1 by 1, maintaining an XY "shadow" of the last block seen in each square
        // use this to construct a bidirectional supports/supported by graph
        val (xRange, yRange, zRange) = run {
            var minX = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var minY = Int.MAX_VALUE
            var maxY = Int.MIN_VALUE
            var minZ = Int.MAX_VALUE
            var maxZ = Int.MIN_VALUE

            for (block in blockList) {
                minX = min(minX, block.xEdge.first)
                maxX = max(maxX, block.xEdge.last)
                minY = min(minY, block.yEdge.first)
                maxY = max(maxY, block.yEdge.last)
                minZ = min(minZ, block.zEdge.first)
                maxZ = max(maxZ, block.zEdge.last)
            }

            check(minX >= 0 && maxX >= 0 && minY >= 0 && maxY >= 0 && minZ >= 0 && maxZ >= 0)

            Triple(minX..maxX, minY..maxY, minZ..maxZ)
        }

        class SupportDag {
            private val supportsMap = mutableMapOf<Block, MutableSet<Block>>()
            private val supportedByMap = mutableMapOf<Block, MutableSet<Block>>()
            private val finalRestStart = mutableMapOf<Block, Int>()

            val (Block).supportedBlocks: Set<Block>
                get() = supportsMap[this] ?: emptySet()

            val (Block).supportingBlocks: Set<Block>
                get() = supportedByMap[this] ?: emptySet()

            val (Block).finalTop: Int
                get() = (finalRestStart[this] ?: throw IllegalStateException(this@finalTop.toString())) + height

            val blocks: Set<Block>
                get() = finalRestStart.keys

            fun add(supporter: Block, supportee: Block) {
                check(supporter in blocks)
                if (supportee !in supporter.supportedBlocks) {
                    print("\t\t")
                    supporter.colour.printed { print(supporter) }
                    print(" -> ")
                    supportee.colour.printed { println(supportee) }
                }
                supportsMap.computeIfAbsent(supporter) { mutableSetOf() }.add(supportee)
                supportedByMap.computeIfAbsent(supportee) { mutableSetOf() }.add(supporter)
                val finalRest = supporter.finalTop + 1
                finalRestStart[supportee]?.let { check(it == finalRest) }
                finalRestStart[supportee] = finalRest
            }

            fun addBase(block: Block) {
                print("\t\tGGGGGGGGGGG -> ")
                block.colour.printed { println(block) }
                finalRestStart[block] = 1
            }
        }

        class Shadow {
            private val cells: MutableList<Block?> = MutableList(xRange.count() * yRange.count()) { null }

            private fun getIdx(x: Int, y: Int): Int = (x - xRange.first) * yRange.count() + (y - yRange.first)

            operator fun get(x: Int, y: Int): Block? = cells[getIdx(x, y)]

            operator fun set(x: Int, y: Int, block: Block?) {
                cells[getIdx(x, y)] = block
            }
        }

        val shadow = Shadow()
        val dag = SupportDag()

        val blocksAtLevel = sequence {
            val queue = PriorityQueue<Block>(compareBy { it.bottom })
            queue.addAll(blockList)
            for (z in zRange) {
                val blockSeq = sequence {
                    while (queue.peek()?.bottom == z) {
                        yield(queue.poll())
                    }
                }
                yield(z to blockSeq)
            }
        }

        fun Int.formatSpc() = "%3d".format(this)

        for ((z, zBlocks) in blocksAtLevel) {
            println("Z=$z")
            zBlocks.forEach { block ->
                block.colour.printed {
                    println("\t$block")
                }
                val candidates = mutableSetOf<Block>()
                block.xEdge.forEach { x ->
                    block.yEdge.forEach { y ->
                        shadow[x, y]?.let { candidates.add(it) }
                        shadow[x, y] = block
                    }
                }
                with(dag) {
                    if (candidates.isEmpty()) {
                        addBase(block)
                    } else {
                        val maxTop = candidates.maxOf { it.finalTop }
                        for (c in candidates) {
                            if (c.finalTop == maxTop) {
                                add(c, block)
                            }
                        }
                    }
                }
            }
            println()
            with(dag) {
                yRange.reversed().forEach { y ->
                    print('\t')
                    print('|')
                    xRange.forEach { x ->
                        val b = shadow[x, y]
                        if (b != null) {
                            b.colour.printed { print(b.finalTop.formatSpc()) }
                        } else {
                            print("   ")
                        }
                        print('|')
                    }
                    println()
                }
            }
            println()
        }

        with(dag) {
            blocks.count { block ->
                // any blocks supported by this block must not only be supported by this block
                block.supportedBlocks.all { supportedBlock ->
                    check(block in supportedBlock.supportingBlocks)
                    supportedBlock.supportingBlocks.size > 1
                }
            }
        }
    }
}
