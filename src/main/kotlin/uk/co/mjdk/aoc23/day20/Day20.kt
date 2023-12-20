package uk.co.mjdk.aoc23.day20

import uk.co.mjdk.aoc.aoc
import java.util.*
import kotlin.collections.LinkedHashSet

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
}

private sealed class ModuleImpl(override val id: String) : Module {
    protected val outputs: SequencedSet<String> = LinkedHashSet()

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
}

fun main() = aoc(2023, 20, { it.lines().map(ConfigLine::parse) }) {
    part1 { configLines ->
        // keep button implicit
        val modules = run {
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
            modules
        }

        var numHigh = 0L
        var numLow = 0L

        data class Signal(val from: String, val to: String, val pulse: Pulse)

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
}
