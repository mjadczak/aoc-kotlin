package uk.co.mjdk.aoc22.day16

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import uk.co.mjdk.aoc.aocReader
import java.lang.Integer.min

data class Valve(val id: String, val flow: Int, val destinations: Set<String>)

val inputPat = Regex("""Valve (\w\w) has flow rate=(\d+); tunnels? leads? to valves? (.+)$""")

fun getValves(): Map<String, Valve> = aocReader(22, 16).useLines { lines ->
    lines.map { line ->
        val (id, flow, rest) = inputPat.matchEntire(line)!!.groupValues.drop(1)
        id to Valve(id, flow.toInt(), rest.split(", ").toSet())
    }.toMap()
}

fun simulate(timeLimit: Int, isElephant: Boolean) {
    // Use Floyd-Warshall to get all pairs of shortest paths
    // Discard any flow=0 nodes
    // Use a DFS to explore all possibilities while being able to one-hop between nodes

    // This could undoubtedly be optimised further, which would be particularly beneficial for Part 2 which considers
    // the square of all possibilities. But it runs fast enough to get the answer, and I'm behind on questions already.

    val valves = getValves()
    val keyList = valves.keys.toList().sorted()
    val noFlow = valves.filterValues { it.flow == 0 }.keys
    val distMap = keyList.asSequence().flatMap { a -> keyList.asSequence().map { b -> a to b } }
        .map { it to Int.MAX_VALUE / 4 }.toMap().toMutableMap()
    for (valve in valves) {
        distMap[valve.key to valve.key] = 0
        valve.value.destinations.forEach { dest ->
            distMap[valve.key to dest] = 1
        }
    }
    for (k in keyList) {
        for (i in keyList) {
            for (j in keyList) {
                if (distMap[i to j]!! > distMap[i to k]!! + distMap[k to j]!!) {
                    distMap[i to j] = distMap[i to k]!! + distMap[k to j]!!
                }
            }
        }
    }
    fun shouldRemove(key: String): Boolean = key != "AA" && key in noFlow
    distMap.keys.removeIf { shouldRemove(it.first) || shouldRemove(it.second) || it.first == it.second }

    assert(valves["AA"]!!.flow == 0)

    fun search(
        currentNode: String,
        currentTime: Int,
        startedValves: PersistentMap<String, Int>
    ): Sequence<PersistentMap<String, Int>> {
        val destinations = distMap.entries.filter { it.key.first == currentNode }
        return sequence {
            yield(startedValves)
            for (dest in destinations) {
                if (dest.key.second in startedValves) {
                    continue
                }
                val openTime = currentTime + dest.value + 1
                if (openTime >= timeLimit) {
                    continue
                }
                yieldAll(search(dest.key.second, openTime, startedValves.put(dest.key.second, openTime)))
            }
        }
    }

    fun evaluate(startedValves: Map<String, Int>): Int = startedValves.map { (key, openTime) ->
        val timeOpen = timeLimit - openTime
        valves[key]!!.flow * timeOpen
    }.sum()

    fun evaluate(startedValves1: Map<String, Int>, startedValves2: Map<String, Int>): Int =
        (startedValves1.keys + startedValves2.keys).map { key ->
            val openTime = if (key !in startedValves2) {
                startedValves1[key]!!
            } else if (key !in startedValves1) {
                startedValves2[key]!!
            } else {
                min(startedValves1[key]!!, startedValves2[key]!!)
            }
            val timeOpen = timeLimit - openTime
            valves[key]!!.flow * timeOpen
        }.sum()

    val results =
        if (isElephant) {
            val allPoss = search("AA", 0, persistentHashMapOf("AA" to 0)).toList()
            var done = 0
            allPoss.asSequence().flatMap { me ->
                print("\r${done}/${allPoss.size}")
                done++
                allPoss.asSequence().map { el ->
                    evaluate(me, el)
                }
            }
        } else {
            search("AA", 0, persistentHashMapOf("AA" to 0)).map(::evaluate)
        }
    val bestPressure = results.max()
    println()
    println(bestPressure)
}

fun main() {
    simulate(30, false)
    simulate(26, true)
}
