package uk.co.mjdk.aoc23.day20

import uk.co.mjdk.aoc.aoc
import java.util.*
import kotlin.collections.LinkedHashSet
import kotlin.math.absoluteValue

private enum class ModuleType {
    Broadcast,
    FlipFlop,
    Conjunction
}

private data class ConfigLine(val moduleType: ModuleType, val moduleId: String, val targets: List<String>) {
    companion object {
        fun parse(input: String): ConfigLine {
            val (idT, tStr) = input.split(" -> ")
            val targets = tStr.split(", ")
            return if (idT == "broadcaster") {
                ConfigLine(ModuleType.Broadcast, idT, targets)
            } else {
                val typ = when (idT.first()) {
                    '%' -> ModuleType.FlipFlop
                    '&' -> ModuleType.Conjunction
                    else -> throw IllegalArgumentException(idT)
                }
                ConfigLine(typ, idT.drop(1), targets)
            }
        }
    }
}

private enum class Pulse {
    High,
    Low,
}

// My guess is we'll need some way to capture immutable state to detect cycles in part 2 - but we can add that later
private sealed interface Module {
    val id: String

    fun process(input: String, pulse: Pulse, send: (String, Pulse) -> Unit)

    fun addOutput(id: String)

    fun addInput(id: String)

    // bleh, but need it
    fun outputList(): List<String>
}

private sealed class ModuleImpl(override val id: String) : Module {
    protected val outputs: SequencedSet<String> = LinkedHashSet()

    override fun outputList(): List<String> = outputs.toList()

    override fun addOutput(id: String) {
        outputs.add(id)
    }

    override fun addInput(id: String) {}

    protected fun sendAll(pulse: Pulse, send: (String, Pulse) -> Unit) {
        outputs.forEach { send(it, pulse) }
    }
}

private class Broadcaster(id: String) : ModuleImpl(id) {
    override fun process(input: String, pulse: Pulse, send: (String, Pulse) -> Unit) {
        sendAll(pulse, send)
    }
}

private class FlipFlop(id: String) : ModuleImpl(id) {
    private var isOn: Boolean = false

    override fun process(input: String, pulse: Pulse, send: (String, Pulse) -> Unit) {
        if (pulse == Pulse.Low) {
            isOn = !isOn
            val pulseToSend = if (isOn) Pulse.High else Pulse.Low
            sendAll(pulseToSend, send)
        }
    }

    val stateChar: Char
        get() = if (isOn) '1' else '0'
}

private class Conjunction(id: String) : ModuleImpl(id) {
    private val inputs: MutableMap<String, Pulse> = mutableMapOf()

    override fun addInput(id: String) {
        inputs[id] = Pulse.Low
    }

    override fun process(input: String, pulse: Pulse, send: (String, Pulse) -> Unit) {
        inputs[input] = pulse
        val pulseToSend = if (inputs.values.all { it == Pulse.High }) Pulse.Low else Pulse.High
        sendAll(pulseToSend, send)
    }
}

private class Dummy(override val id: String) : Module {
    override fun process(input: String, pulse: Pulse, send: (String, Pulse) -> Unit) {}

    override fun addOutput(id: String) {}

    override fun addInput(id: String) {}

    override fun outputList(): List<String> = emptyList()
}

fun main() = aoc(2023, 20, { it.lines().map(ConfigLine::parse) }) {
    fun mkModules(configLines: List<ConfigLine>): Map<String, Module> {
        val modules = mutableMapOf<String, Module>()
        val moduleToInputs = mutableMapOf<String, SequencedSet<String>>()
        for (line in configLines) {
            val module = when (line.moduleType) {
                ModuleType.Broadcast -> Broadcaster(line.moduleId)
                ModuleType.FlipFlop -> FlipFlop(line.moduleId)
                ModuleType.Conjunction -> Conjunction(line.moduleId)
            }
            line.targets.forEach {
                module.addOutput(it)
                moduleToInputs.computeIfAbsent(it) { LinkedHashSet() }.add(module.id)
            }
            modules[module.id] = module
        }
        moduleToInputs.forEach { (id, inputs) ->
            val module = modules.computeIfAbsent(id) { Dummy(id) }
            inputs.forEach { module.addInput(it) }
        }
        modules["button"] = Dummy("button")
        return modules
    }

    data class Signal(val from: String, val to: String, val pulse: Pulse)

    part1 { configLines ->
        val modules = mkModules(configLines)

        var numHigh = 0L
        var numLow = 0L

        val queue = ArrayDeque<Signal>()
        fun pressButton() {
            queue.add(Signal("button", "broadcaster", Pulse.Low))

            while (queue.isNotEmpty()) {
                val s = queue.poll()
                if (s.pulse == Pulse.High) {
                    numHigh += 1
                } else if (s.pulse == Pulse.Low) {
                    numLow += 1
                }
                modules[(s.to)]!!.process(s.from, s.pulse) { target, pulse -> queue.add(Signal(s.to, target, pulse)) }
            }
        }

        repeat(1000) { pressButton() }

        numHigh * numLow
    }

    part2 { configLines ->
        val modules = mkModules(configLines)
        val queue = ArrayDeque<Signal>()
        var numPressed = 0L

        // naive simulation is too slow - we need to either programmatically compress the circuit, or inspect what it is that they've implemented manually
        // solution isn't the most general, based on some manual inspection of the graph

        fun graphviz() {
            println("strict digraph G {")
            modules.values.forEach { m ->
                val attrs =
                    when {
                        m is Broadcaster -> mapOf("shape" to "trapezium", "fillcolor" to "red", "label" to m.id)
                        m.id == "rx" -> mapOf("shape" to "invtrapezium", "fillcolor" to "green", "label" to "%rx")
                        m is FlipFlop -> mapOf(
                            "shape" to "hexagon",
                            "fillcolor" to "blue",
                            "fontcolor" to "white",
                            "label" to "%${m.id}"
                        )

                        m is Conjunction -> mapOf("shape" to "rect", "fillcolor" to "yellow", "label" to "&${m.id}")
                        m.id == "button" -> mapOf("shape" to "circle", "fillcolor" to "pink", "label" to m.id)
                        else -> TODO()
                    }
                val outputs = if (m.id == "button") listOf("broadcaster") else m.outputList()
                println("${m.id} [style=filled,${attrs.entries.joinToString(",") { (k, v) -> """$k="$v"""" }}]")
                println("${m.id} -> {${outputs.joinToString(" ")}}")
            }
            println("}")
        }

        // by inspection (annoyingly), the broadcast feeds four disjoint clusters of flip flops + a conjunction, whose output is then inverted into a final conjunction feeding %rx.
        // we expect those to cycle with some frequency, and then the point we're looking for is when those cycles sync up.
        // in theory we should have to keep track of the state of all the flip flops in the cluster, but maybe there's just one blip per cycle...

        val rxFeedId = modules.values.find { it.outputList() == listOf("rx") }!!.id
        val feedFeeders = modules.values.filter { it.outputList().contains(rxFeedId) }.map { it.id }
            .associateWith { mutableListOf<Long>() }

        while (feedFeeders.values.any { it.size <= 5 }) {
            numPressed += 1
            queue.add(Signal("button", "broadcaster", Pulse.Low))

            while (queue.isNotEmpty()) {
                val s = queue.poll()
                if (s.from in feedFeeders && s.pulse == Pulse.High) {
                    feedFeeders[s.from]!!.add(numPressed)
                }
                modules[(s.to)]!!.process(s.from, s.pulse) { target, pulse -> queue.add(Signal(s.to, target, pulse)) }
            }
        }

        val cycleLengths = feedFeeders.mapValues {
            it.value.zipWithNext { l, r -> r - l }.distinct().let { check(it.size == 1); it.first() }
        }

        fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

        fun lcm(a: Long, b: Long): Long = a.absoluteValue * b.absoluteValue / gcd(a, b)

        cycleLengths.values.reduce(::lcm)
    }
}
