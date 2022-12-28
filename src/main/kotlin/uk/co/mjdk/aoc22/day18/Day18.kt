package uk.co.mjdk.aoc22.day18

import uk.co.mjdk.aoc.aocInput

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

fun getVoxels(): Set<Voxel> = aocInput(22, 18).useLines { lines ->
    lines.map { line ->
        val (x, y, z) = line.split(',').map { it.toInt() }
        Voxel(x, y, z)
    }.toSet()
}

fun main() {
    val voxels = getVoxels()
    val voxelToSides = voxels.associateWith {
        it.neighbours().count { n -> n in voxels }
    }
    println(voxelToSides.values.sumOf { 6 - it })
}
