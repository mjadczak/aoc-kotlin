package uk.co.mjdk.aoc22.day18

import uk.co.mjdk.aoc.aocReader

data class Voxel(val x: Int, val y: Int, val z: Int) {
    fun neighbours(): Sequence<Voxel> = sequenceOf(
        copy(x = x - 1),
        copy(x = x + 1),
        copy(y = y + 1),
        copy(y = y - 1),
        copy(z = z + 1),
        copy(z = z - 1)
    )
}

fun getVoxels(): Set<Voxel> = aocReader(22, 18).useLines { lines ->
    lines.map { line ->
        val (x, y, z) = line.split(',').map { it.toInt() }
        Voxel(x, y, z)
    }.toSet()
}

fun part1() {
    val voxels = getVoxels()
    val voxelToSides = voxels.associateWith {
        it.neighbours().count { n -> n in voxels }
    }
    println(voxelToSides.values.sumOf { 6 - it })
}

fun part2() {
    val voxels = getVoxels()
    val minX = voxels.minOf { it.x }
    val maxX = voxels.maxOf { it.x }
    val minY = voxels.minOf { it.y }
    val maxY = voxels.maxOf { it.y }
    val minZ = voxels.minOf { it.z }
    val maxZ = voxels.maxOf { it.z }

    // iterate through the bounding box, and for each empty voxel try to search to find the size of the empty space
    // if we ever try to visit something outside the BB, we're on the outside and we abort

    val visited = mutableSetOf<Voxel>()

    fun searchSpace(pos: Voxel, currentSet: MutableSet<Voxel> = mutableSetOf()): Set<Voxel>? {
        visited.add(pos)
        currentSet.add(pos)
        for (n in pos.neighbours()) {
            if (n in currentSet) {
                continue
            }
            if (n.x !in minX..maxX || n.y !in minY..maxY || n.z !in minZ..maxZ || n in visited) {
                return null // abort
            }
            if (n in voxels) {
                continue
            }
            searchSpace(n, currentSet) ?: return null
        }
        return currentSet
    }

    val internalVoxels = mutableSetOf<Voxel>()
    for (x in minX..maxX) {
        for (y in minY..maxY) {
            for (z in minZ..maxZ) {
                val startPos = Voxel(x, y, z)
                if (startPos in visited || startPos in voxels) {
                    continue
                }
                searchSpace(startPos)?.let { internalVoxels.addAll(it) }
            }
        }
    }

    val totalSurface = voxels.map { it.neighbours().count { n -> n in voxels } }.sumOf { 6 - it }

    val internalSurface = internalVoxels.map { it.neighbours().count { n -> n in voxels } }.sumOf { it }

    println(totalSurface - internalSurface)
}

fun main() {
    part1()
    part2()
}
