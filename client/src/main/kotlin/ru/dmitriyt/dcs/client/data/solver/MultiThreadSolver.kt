package ru.dmitriyt.dcs.client.data.solver

import ru.dmitriyt.dcs.client.domain.solver.TaskSolver
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.core.domain.GraphTask
import kotlin.concurrent.thread

class MultiThreadSolver(private val graphTask: GraphTask) : TaskSolver {

    override fun run(inputProvider: () -> Task, resultHandler: (TaskResult) -> Unit, onFinish: () -> Unit) {
        val nCpu = Runtime.getRuntime().availableProcessors()
        val threads = IntRange(0, nCpu).map {
            thread {
                SingleSolver(graphTask).run(inputProvider, resultHandler, onFinish)
            }
        }
        threads.map { it.join() }
    }
}