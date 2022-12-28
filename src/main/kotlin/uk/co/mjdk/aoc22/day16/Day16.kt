package uk.co.mjdk.aoc22.day16

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentHashMapOf
import uk.co.mjdk.aoc.aocInput

data class Valve(val id: String, val flow: Int, val destinations: Set<String>)

val inputPat = Regex("""Valve (\w\w) has flow rate=(\d+); tunnels? leads? to valves? (.+)$""")

fun getValves(): Map<String, Valve> = aocInput(22, 16).useLines { lines ->
    lines.map { line ->
        val (id, flow, rest) = inputPat.matchEntire(line)!!.groupValues.drop(1)
        id to Valve(id, flow.toInt(), rest.split(", ").toSet())
    }.toMap()
}

fun main() {
    // Use Floyd-Warshall to get all pairs of shortest paths
    // Discard any flow=0 nodes
    // Use a DFS to explore all possibilities while being able to one-hop between nodes

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
                if (openTime >= 30) {
                    continue
                }
                yieldAll(search(dest.key.second, openTime, startedValves.put(dest.key.second, openTime)))
            }
        }
    }

    fun evaluate(startedValves: PersistentMap<String, Int>): Int = startedValves.map { (key, openTime) ->
        val timeOpen = 30 - openTime
        valves[key]!!.flow * timeOpen
    }.sum()

    val bestPressure = search("AA", 0, persistentHashMapOf("AA" to 0)).map(::evaluate).max()
    println(bestPressure)
}
