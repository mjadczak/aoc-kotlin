package uk.co.mjdk.aoc22.day07

import uk.co.mjdk.aoc.aocInput

data class File(val name: String, val size: Int)
data class Directory(val name: String, val childDirs: List<Directory>, val childFiles: List<File>) {
    val size: Int by lazy {
        childFiles.sumOf { it.size } + childDirs.sumOf { it.size }
    }

    fun allDirectories(): Sequence<Directory> =
        childDirs.asSequence().flatMap { it.allDirectories() } + sequenceOf(this)
}

sealed interface Instruction {
    data class Cd(val dir: String) : Instruction
    object Ls : Instruction
    data class File(val name: String, val size: Int) : Instruction
    data class Dir(val name: String) : Instruction

    companion object {
        fun parse(line: String): Instruction {
            if (line == "$ ls") {
                return Ls
            }
            if (line.startsWith("$ cd ")) {
                return Cd(line.replaceFirst("$ cd ", ""))
            }
            if (line.startsWith("dir ")) {
                return Dir(line.split(' ')[1])
            }
            val (size, name) = line.split(' ')
            return File(name, size.toInt())
        }
    }
}

fun parseDirectory(name: String, iterator: Iterator<Instruction>): Directory {
    assert(iterator.next() == Instruction.Ls)
    val files = mutableListOf<File>()
    val pendingDirs = mutableSetOf<String>()
    val dirs = mutableListOf<Directory>()
    for (instr in iterator) {
        when (instr) {
            is Instruction.File -> files.add(File(instr.name, instr.size))
            is Instruction.Dir -> pendingDirs.add(instr.name)
            is Instruction.Cd ->
                if (instr.dir == "..") {
                    break
                } else {
                    assert(pendingDirs.remove(instr.dir))
                    dirs.add(parseDirectory(instr.dir, iterator))
                }

            is Instruction.Ls ->
                throw IllegalStateException("Did not expect second Ls for a directory")
        }
    }

    assert(pendingDirs.isEmpty())
    return Directory(name, dirs.toList(), files.toList())
}

fun parseRoot(): Directory {
    aocInput(22, 7).useLines { lines ->
        val instructionsIter = lines.map(Instruction::parse).iterator()
        assert(instructionsIter.next() == Instruction.Cd("/"))
        return parseDirectory("/", instructionsIter)
    }
}

fun main() {
    val root = parseRoot()

    // Part 1
    val smallDirSize = root.allDirectories().filter { it.size <= 100000 }.sumOf { it.size }
    println(smallDirSize)

    // Part 2
    val unusedSpace = 70000000 - root.size
    val requiredSpace = 30000000 - unusedSpace
    val smallestPossibleDir = root.allDirectories().filter { it.size >= requiredSpace }.minBy { it.size }
    println(smallestPossibleDir.size)
}
