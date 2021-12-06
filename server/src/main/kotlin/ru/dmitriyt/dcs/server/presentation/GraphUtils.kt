package ru.dmitriyt.dcs.server.presentation

import ru.dmitriyt.dcs.server.logd
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.streams.toList

object GraphUtils {
    fun getGraphsCount(n: Int, args: String): Int {
        val commandList = listOfNotNull(
            "./geng",
            args.takeIf { it.isNotEmpty() },
            n.toString(),
            "-u",
        )
        logd("command to get count: ${commandList.joinToString(" ")}")
        val process = ProcessBuilder(*commandList.toTypedArray())
            .directory(File(System.getProperty("user.dir")))
            .start()

        val reader = BufferedReader(
            InputStreamReader(
                process.errorStream
            )
        )
        val lines = reader.lines().toList()
        logd("count result start")
        logd(lines.joinToString("\n"))
        logd("count result end")
        return lines.find { it.startsWith(">Z") }?.let { line ->
            line.split(" ").getOrNull(1)?.toIntOrNull()
        } ?: 0
    }
}