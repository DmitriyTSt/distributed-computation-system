package ru.dmitriyt.dcs.client.domain.solver

import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult

interface TaskSolver {
    fun run(
        inputProvider: () -> Task,
        resultHandler: (TaskResult) -> Unit,
        onFinish: () -> Unit
    )
}