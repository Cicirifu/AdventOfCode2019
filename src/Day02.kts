
import java.io.File

class CPU(val memory: IntArray, var ip: Int = 0) {
    object Opcodes {
        const val ADD = 1
        const val MUL = 2
        const val HLT = 99
    }

    private val terminated get() = ip < 0
    private fun get() = memory[ip++]

    fun run() {
        while (!terminated) {
            step()
        }
    }

    fun step() {
        require(!terminated)

        when (/* opcode = */ get()) {
            Opcodes.ADD -> {
                val a = get()
                val b = get()
                val t = get()

                memory[t] = memory[a] + memory[b]
            }
            Opcodes.MUL -> {
                val a = get()
                val b = get()
                val t = get()

                memory[t] = memory[a] * memory[b]
            }
            Opcodes.HLT -> {
                ip = -1
            }
            else -> throw UnsupportedOperationException()
        }
    }
}

val initialMemory by lazy {
    File("Day02.txt").readLines(Charsets.UTF_8).asSequence()
        .flatMap { it.splitToSequence(",") }
        .map { it.toInt() }
        .toList().toIntArray()
}

val runtimeMemory = initialMemory.copyOf().apply {
    this[1] = 12
    this[2] = 2
}

val cpu = CPU(runtimeMemory)
cpu.run()
println("Assignment A: ${cpu.memory[0]}")
require(cpu.memory[0] == 7210630)

for (verb in 0 .. 99) { outer@
    for (noun in 0 .. 99) {
        initialMemory.copyInto(runtimeMemory).apply {
            this[1] = noun
            this[2] = verb
        }
        val cpu = CPU(runtimeMemory)
        cpu.run()
        if (cpu.memory[0] == 19690720) {
            val formula = 100 * noun + verb
            println("Assignment B: $formula")
            require(formula == 3892)
            break@outer
        }
    }
}