package uk.co.mjdk.aoc22.day08

import uk.co.mjdk.aoc.aocInput
import kotlin.math.max

class Visibility(
    var fromTop: Boolean = false,
    var fromRight: Boolean = false,
    var fromBottom: Boolean = false,
    var fromLeft: Boolean = false
) {
    fun isVisible(): Boolean = fromTop || fromRight || fromBottom || fromLeft
}

class MaxHeight(var fromTop: Int = 0, var fromRight: Int = 0, var fromBottom: Int = 0, var fromLeft: Int = 0)

data class Tree(val height: Int, val visibility: Visibility = Visibility(), val maxHeight: MaxHeight = MaxHeight())

fun parsePatch(): List<List<Tree>> {
    aocInput(22, 8).useLines { lines ->
        return lines.map { line ->
            line.map { Tree(it.digitToInt()) }.toList()
        }.toList()
    }
}

fun main() {
    val patch = parsePatch()
    val rows = patch.count()
    val cols = patch[0].count()

    // First iteration, top-left to bottom-right. Check top and left visibility.
    (0 until rows).forEach { row ->
        (0 until cols).forEach { col ->
            val tree = patch[row][col]
            if (row == 0) {
                tree.maxHeight.fromTop = tree.height
                tree.visibility.fromTop = true
            } else {
                val top = patch[row - 1][col]
                tree.maxHeight.fromTop = max(top.maxHeight.fromTop, tree.height)
                tree.visibility.fromTop = tree.height > top.maxHeight.fromTop
            }

            if (col == 0) {
                tree.maxHeight.fromLeft = tree.height
                tree.visibility.fromLeft = true
            } else {
                val left = patch[row][col - 1]
                tree.maxHeight.fromLeft = max(left.maxHeight.fromLeft, tree.height)
                tree.visibility.fromLeft = tree.height > left.maxHeight.fromLeft
            }
        }
    }

    // Second iteration, bottom-right to top-left. Check bottom and right visibility.
    (0 until rows).reversed().forEach { row ->
        (0 until cols).reversed().forEach { col ->
            val tree = patch[row][col]
            if (row == rows - 1) {
                tree.maxHeight.fromBottom = tree.height
                tree.visibility.fromBottom = true
            } else {
                val bottom = patch[row + 1][col]
                tree.maxHeight.fromBottom = max(bottom.maxHeight.fromBottom, tree.height)
                tree.visibility.fromBottom = tree.height > bottom.maxHeight.fromBottom
            }

            if (col == cols - 1) {
                tree.maxHeight.fromRight = tree.height
                tree.visibility.fromRight = true
            } else {
                val right = patch[row][col + 1]
                tree.maxHeight.fromRight = max(right.maxHeight.fromRight, tree.height)
                tree.visibility.fromRight = tree.height > right.maxHeight.fromRight
            }
        }
    }

    val numVisible = patch.asSequence().flatten().count { it.visibility.isVisible() }
    println(numVisible)
}
