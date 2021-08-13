package ru.dmitriyt.dcs.client.data.solver

import ru.dmitriyt.dcs.client.domain.solver.TaskSolver
import ru.dmitriyt.dcs.core.GraphTask
import ru.dmitriyt.dcs.core.data.GraphResult
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult

class SingleSolver(private val graphTask: GraphTask) : TaskSolver {

    override fun run(inputProvider: () -> Task, resultHandler: (TaskResult) -> Unit, onFinish: () -> Unit) {
        var task = inputProvider()
        while (task.graphs.isNotEmpty()) {
            resultHandler(
                TaskResult(
                    task.id,
                    task.graphs.map { GraphResult(it, graphTask.solve(it)) }
                )
            )
            task = inputProvider()
        }
        onFinish()
    }
}