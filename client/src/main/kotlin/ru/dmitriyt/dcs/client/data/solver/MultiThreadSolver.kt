package ru.dmitriyt.dcs.client.data.solver

import ru.dmitriyt.dcs.client.domain.GraphTaskInfo
import ru.dmitriyt.dcs.client.domain.solver.TaskSolver
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import kotlin.concurrent.thread

/**
 * Многопоточный вычислитель
 */
class MultiThreadSolver(private val graphTaskInfo: GraphTaskInfo) : TaskSolver {

    override fun run(inputProvider: () -> Task, resultHandler: (TaskResult) -> Unit, onFinish: () -> Unit) {
        val nCpu = Runtime.getRuntime().availableProcessors()
        val threads = IntRange(0, nCpu).map {
            thread {
                SingleSolver(graphTaskInfo).run(inputProvider, resultHandler, onFinish)
            }
        }
        threads.map { it.join() }
    }
}