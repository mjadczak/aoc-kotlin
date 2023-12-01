package uk.co.mjdk.aoc22.day19

import uk.co.mjdk.aoc.aocInputStored

val inputPat =
    Regex("""Blueprint (\d+): Each ore robot costs (\d+) ore. Each clay robot costs (\d+) ore. Each obsidian robot costs (\d+) ore and (\d+) clay. Each geode robot costs (\d+) ore and (\d+) obsidian.""")

data class Blueprint(
    val id: Int,
    val oreRobotOreCost: Int,
    val clayRobotOreCost: Int,
    val obsidianRobotOreCost: Int,
    val obsidianRobotClayCost: Int,
    val geodeRobotOreCost: Int,
    val geodeRobotObsidianCost: Int
)

data class Resources(
    val ore: Int = 0,
    val clay: Int = 0,
    val obsidian: Int = 0,
    val geode: Int = 0
)

data class Robots(
    val ore: Int = 0,
    val clay: Int = 0,
    val obsidian: Int = 0,
    val geode: Int = 0
)

data class State(
    val resources: Resources,
    val robots: Robots,
    val isBuilding: RobotType?
) {
    fun canBuild(robotType: RobotType, blueprint: Blueprint): Boolean =
        when (robotType) {
            RobotType.Ore -> resources.ore >= blueprint.oreRobotOreCost
            RobotType.Clay -> resources.ore >= blueprint.clayRobotOreCost
            RobotType.Obsidian -> resources.ore >= blueprint.obsidianRobotOreCost && resources.clay >= blueprint.obsidianRobotClayCost
            RobotType.Geode -> resources.ore >= blueprint.geodeRobotOreCost && resources.obsidian >= blueprint.geodeRobotObsidianCost
        }

    fun startBuild(robotType: RobotType, blueprint: Blueprint): State {
        assert(canBuild(robotType, blueprint))
        assert(isBuilding == null)
        return copy(
            isBuilding = robotType,
            resources = when (robotType) {
                RobotType.Ore -> resources.copy(ore = resources.ore - blueprint.oreRobotOreCost)
                RobotType.Clay -> resources.copy(ore = resources.ore - blueprint.clayRobotOreCost)
                RobotType.Obsidian -> resources.copy(
                    ore = resources.ore - blueprint.obsidianRobotOreCost,
                    clay = resources.clay - blueprint.obsidianRobotClayCost
                )

                RobotType.Geode -> resources.copy(
                    ore = resources.ore - blueprint.geodeRobotOreCost,
                    obsidian = resources.obsidian - blueprint.geodeRobotObsidianCost
                )
            }
        )
    }

    fun finishBuild(): State {
        if (isBuilding == null) {
            throw IllegalStateException()
        }
        return copy(
            robots = when (isBuilding) {
                RobotType.Ore -> robots.copy(ore = robots.ore + 1)
                RobotType.Clay -> robots.copy(clay = robots.clay + 1)
                RobotType.Obsidian -> robots.copy(obsidian = robots.obsidian + 1)
                RobotType.Geode -> robots.copy(geode = robots.geode + 1)
            },
            isBuilding = null
        )
    }

    fun collect(): State = copy(
        resources = resources.copy(
            ore = resources.ore + robots.ore,
            clay = resources.clay + robots.clay,
            obsidian = resources.obsidian + robots.obsidian,
            geode = resources.geode + robots.geode
        )
    )

    fun advance(action: Action, blueprint: Blueprint): State {
        val robot = when (action) {
            Action.Wait -> null
            Action.BuildOre -> RobotType.Ore
            Action.BuildClay -> RobotType.Clay
            Action.BuildObsidian -> RobotType.Obsidian
            Action.BuildGeode -> RobotType.Geode
        }
        return if (robot == null) {
            collect()
        } else {
            startBuild(robot, blueprint).collect().finishBuild()
        }
    }

    fun canDo(action: Action, blueprint: Blueprint): Boolean = when(action) {
        Action.Wait -> true
        Action.BuildOre -> canBuild(RobotType.Ore, blueprint)
        Action.BuildClay -> canBuild(RobotType.Clay, blueprint)
        Action.BuildObsidian -> canBuild(RobotType.Obsidian, blueprint)
        Action.BuildGeode -> canBuild(RobotType.Geode, blueprint)
    }

    companion object {
        val initial = State(
            robots = Robots(ore = 1),
            resources = Resources(),
            isBuilding = null
        )
    }
}

enum class Action {
    Wait,
    BuildOre,
    BuildClay,
    BuildObsidian,
    BuildGeode
}

enum class RobotType {
    Ore,
    Clay,
    Obsidian,
    Geode
}

fun getBlueprints(): Map<Int, Blueprint> = aocInputStored(22, 19).useLines { lines ->
    lines.map { line ->
        val vals = inputPat.matchEntire(line)!!.groupValues.drop(1).map { it.toInt() }
        vals[0] to Blueprint(vals[0], vals[1], vals[2], vals[3], vals[4], vals[5], vals[6])
    }.toMap()
}

fun bestGeodes(blueprint: Blueprint): Int {
    fun search(state: State, timeRemaining: Int): Sequence<Int> = sequence {
        if (timeRemaining == 0) {
            yield(state.resources.geode)
            return@sequence
        }

        sequenceOf(Action.Wait, Action.BuildOre, Action.BuildClay, Action.BuildObsidian, Action.BuildGeode)
            .filter { state.canDo(it, blueprint) }
            .flatMap {
                search(state.advance(it, blueprint), timeRemaining - 1)
            }
            .let {
                yieldAll(it)
            }
    }

    return search(State.initial, 24).max()
}

fun main() {
    val total = getBlueprints().map {
        it.key * bestGeodes(it.value)
    }.onEach { print(".") }.sum()
    println(total)
}
