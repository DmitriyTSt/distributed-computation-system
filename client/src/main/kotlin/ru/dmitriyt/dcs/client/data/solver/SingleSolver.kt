package ru.dmitriyt.dcs.client.data.solver

import ru.dmitriyt.dcs.client.domain.GraphTaskInfo
import ru.dmitriyt.dcs.client.domain.solver.TaskSolver
import ru.dmitriyt.dcs.client.logd
import ru.dmitriyt.dcs.core.data.DefaultConfig
import ru.dmitriyt.dcs.core.data.GraphResult
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.streams.toList

/**
 * Однопоточный вычислитель
 */
class SingleSolver(private val graphTaskInfo: GraphTaskInfo) : TaskSolver {

    override fun run(inputProvider: () -> Task, resultHandler: (TaskResult) -> Unit, onFinish: () -> Unit) {
        logd("run $this")
        var task = inputProvider()
        logd("run $task")
        var graphs = getGraphsFromTask(task)
        while (!task.isSpecialEmpty) {
            resultHandler(
                when (graphTaskInfo) {
                    is GraphTaskInfo.Invariant -> TaskResult.Invariant(
                        task.id,
                        graphs.size,
                        graphs.map { GraphResult(it, graphTaskInfo.task.solve(it)) },
                    )
                    is GraphTaskInfo.Condition -> TaskResult.Graphs(
                        task.id,
                        graphs.size,
                        graphs.filter { graphTaskInfo.task.check(it) },
                    )
                }

            )
            task = inputProvider()
            logd("run $task")
            graphs = getGraphsFromTask(task)
        }
        onFinish()
    }

    private fun getGraphsFromTask(task: Task): List<String> {
        logd("get graphs from task $task")
        val list = task.graphs.ifEmpty {
            val graphs = mutableListOf<String>()
            if (task.partNumber < task.partsCount) {
                try {
                    val commandList = listOfNotNull(
                        "./geng",
                        task.args.takeIf { it.isNotEmpty() },
                        task.n.toString(),
                        "${task.partNumber}/${task.partsCount}",
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
            graphs
        }
        logd("task ${task.id} size = ${list.size}")
        return list
    }
}