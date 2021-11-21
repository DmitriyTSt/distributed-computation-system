package ru.dmitriyt.dcs.client.data.solver

import ru.dmitriyt.dcs.client.domain.GraphTaskInfo
import ru.dmitriyt.dcs.client.domain.solver.TaskSolver
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
//        println("run $this")
        var task = inputProvider()
//        println("run $task")
        var graphs = getGraphsFromTask(task)
        while (graphs.isNotEmpty()) {
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
//            println("run $task")
            graphs = getGraphsFromTask(task)
        }
        onFinish()
    }

    private fun getGraphsFromTask(task: Task): List<String> {
//        println("get graphs from task ${task}")
        val list = task.graphs.ifEmpty {
            val graphs = mutableListOf<String>()
            if (task.partNumber < 100) {
                try {
                    val process = ProcessBuilder("./geng", "${task.n}", "${task.partNumber}/100")
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
//        println("task ${task.id} size = ${list.size}")
        return list
    }
}