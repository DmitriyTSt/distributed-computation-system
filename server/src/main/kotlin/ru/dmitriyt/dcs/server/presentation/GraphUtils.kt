package ru.dmitriyt.dcs.server.presentation

import ru.dmitriyt.dcs.core.data.DefaultConfig
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.proto.GraphTaskProto
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

    fun getGraphs(task: GraphTaskProto.Task): List<String> {
        val graphs = mutableListOf<String>()
        if (task.partNumber < DefaultConfig.GENG_PARTS_COUNT) {
            try {
                val commandList = listOfNotNull(
                    "./geng",
                    task.args.takeIf { it.isNotEmpty() },
                    task.n.toString(),
                    "${task.partNumber}/${DefaultConfig.GENG_PARTS_COUNT}",
                )
                logd("generate command: ${commandList.joinToString(" ")}")
                val process = ProcessBuilder(*commandList.toTypedArray())
                    .directory(File(System.getProperty("user.dir"))).start()

                val reader = BufferedReader(
                    InputStreamReader(
                        process.inputStream
                    )
                )
                graphs.addAll(reader.lines().toList().filter { !it.startsWith(">") })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return graphs
    }
}