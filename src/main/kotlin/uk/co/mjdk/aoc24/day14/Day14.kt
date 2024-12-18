package uk.co.mjdk.aoc24.day14

import uk.co.mjdk.aoc.aoc
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.image.BufferedImage
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities
import javax.swing.Timer
import javax.swing.WindowConstants.EXIT_ON_CLOSE
import kotlin.text.appendLine

fun Long.posMod(other: Long): Long {
    val res = this % other
    return if (res < 0L) res + other else res
}

data class Vector(val x: Long, val y: Long) {
    operator fun plus(v: Vector) = Vector(x + v.x, y + v.y)
    operator fun minus(v: Vector) = Vector(x - v.x, y - v.y)
    operator fun times(scalar: Long) = Vector(x * scalar, y * scalar)
    fun constrain(v: Vector) = Vector(x.posMod(v.x), y.posMod(v.y))
}

data class Robot(val position: Vector, val velocity: Vector) {
    fun positionAfter(seconds: Long): Vector = position + velocity * seconds
    fun after(seconds: Long, extents: Vector): Robot = copy(position = positionAfter(seconds).constrain(extents))
    fun afterSecond(extents: Vector): Robot = after(1, extents)

    companion object {
        private val pat = Regex("""p=(-?\d+),(-?\d+) v=(-?\d+),(-?\d+)""")
        fun parse(input: String): Robot {
            return pat.matchEntire(input)?.destructured?.let { (px, py, vx, vy) ->
                Robot(Vector(px.toLong(), py.toLong()), Vector(vx.toLong(), vy.toLong()))
            } ?: throw IllegalArgumentException("Invalid input $input")
        }
    }
}

enum class VerticalHalf {
    Left, Right
}

enum class HorizontalHalf {
    Top, Bottom
}

data class Quadrant(val v: VerticalHalf, val h: HorizontalHalf)

fun render(extents: Vector, positions: List<Vector>): String = buildString {
    val pos = positions.groupingBy { it }.eachCount()
    (0..<extents.y).forEach { y ->
        (0..<extents.x).forEach { x ->
            val coord = Vector(x, y)
            val count = pos[coord]
            when {
                count == null -> '.'
                count > 9 -> 'X'
                else -> count.digitToChar()
            }.let(::append)
        }
        appendLine()
    }
}

fun main() = aoc(2024, 14, { it.lines().map(Robot::parse) }) {
    example(
        """
        p=0,4 v=3,-3
        p=6,3 v=-1,-3
        p=10,3 v=-1,2
        p=2,0 v=2,-1
        p=0,0 v=1,3
        p=3,0 v=-2,-2
        p=7,6 v=-1,-3
        p=3,0 v=-1,-2
        p=9,3 v=2,3
        p=7,3 v=-1,2
        p=2,4 v=2,-3
        p=9,5 v=-3,-3
    """.trimIndent()
    )

    part1 { robots ->
//        val extents = Vector(11L, 7L)
        val extents = Vector(101L, 103L)
        val positions = robots.map { robot ->
            robot.positionAfter(100).constrain(extents)
        }
        val middle = Vector((extents.x / 2), (extents.y / 2))
        positions.mapNotNull { pos ->
            val v = when {
                pos.x < middle.x -> VerticalHalf.Left
                pos.x > middle.x -> VerticalHalf.Right
                else -> null
            }
            val h = when {
                pos.y < middle.y -> HorizontalHalf.Top
                pos.y > middle.y -> HorizontalHalf.Bottom
                else -> null
            }
            if (h != null && v != null) Quadrant(v, h) else null
        }.groupingBy { it }.eachCount().values.reduce(Int::times)
    }

    part2 { robots ->
        val extents = Vector(101L, 103L)
        val frame = JFrame("Day 14").also {
            it.defaultCloseOperation = EXIT_ON_CLOSE
            it.isResizable = false
        }
        val label = JLabel().also {
            it.preferredSize = Dimension(extents.x.toInt(), 20)
            frame.contentPane.add(it, BorderLayout.NORTH)
        }
        val panel = object : JLabel() {
            private val image = BufferedImage(extents.x.toInt(), extents.y.toInt(), BufferedImage.TYPE_INT_ARGB)
            override fun getPreferredSize(): Dimension = Dimension(extents.x.toInt() * 2, extents.y.toInt() * 2)
            override fun paintComponent(g: Graphics?) {
                super.paintComponent(g)
                (g as? Graphics2D)?.let { g2 ->
                    g2.scale(
                        graphicsConfiguration.defaultTransform.scaleX, graphicsConfiguration.defaultTransform.scaleY
                    )
                    g2.drawImage(image, 0, 0, image.width, image.height, this)
                }
            }

            fun paintRobots(robots: List<Robot>) {
                val pos = robots.mapTo(mutableSetOf()) { it.position }
                (0..<extents.y).forEach { y ->
                    (0..<extents.x).forEach { x ->
                        val bg = Color.white.rgb
                        val coord = Vector(x, y)
                        val colour = if (coord in pos) Color.red.rgb else bg
                        image.setRGB(x.toInt(), y.toInt(), colour)
                    }
                }
                repaint()
            }
        }.also {
            frame.contentPane.add(it, BorderLayout.CENTER)
        }
        frame.run {
            setLocationRelativeTo(null)
            pack()
            isVisible = true
            println(size)
        }

        val current = robots.toMutableList()
        var i = 0
        fun advance() {
            label.text = i.toString()
            panel.paintRobots(current)
            current.indices.forEach { r ->
                current[r] = current[r].afterSecond(extents)
            }
            i++
        }

        val initial = 7000
        val final = 20000
        repeat(initial) { advance() }
        panel.addMouseListener(object : MouseListener {
            override fun mouseClicked(e: MouseEvent?) {
                advance()
            }

            override fun mousePressed(e: MouseEvent?) {
            }

            override fun mouseReleased(e: MouseEvent?) {
            }

            override fun mouseEntered(e: MouseEvent?) {
            }

            override fun mouseExited(e: MouseEvent?) {
            }
        })
        val t = Timer(10) { if (i < final) advance() }
        //t.start()

        7083
    }
}
