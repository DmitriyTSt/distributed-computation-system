package ru.dmitriyt.dcs.client.data.solver

import ru.dmitriyt.dcs.client.domain.GraphTaskInfo
import ru.dmitriyt.dcs.client.domain.solver.TaskSolver
import ru.dmitriyt.dcs.core.data.GraphResult
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult

/**
 * Однопоточный вычислитель
 */
class SingleSolver(private val graphTaskInfo: GraphTaskInfo) : TaskSolver {

    override fun run(inputProvider: () -> Task, resultHandler: (TaskResult) -> Unit, onFinish: () -> Unit) {
        var task = inputProvider()
        while (task.graphs.isNotEmpty()) {
            resultHandler(
                when (graphTaskInfo) {
                    is GraphTaskInfo.Invariant -> TaskResult.Invariant(
                        task.id,
                        task.graphs.size,
                        task.graphs.map { GraphResult(it, graphTaskInfo.task.solve(it)) },
                    )
                    is GraphTaskInfo.Condition -> TaskResult.Graphs(
                        task.id,
                        task.graphs.size,
                        task.graphs.filter { graphTaskInfo.task.check(it) },
                    )
                }

            )
            task = inputProvider()
        }
        onFinish()
    }
}