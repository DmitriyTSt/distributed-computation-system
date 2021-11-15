package ru.dmitriyt.dcs.core.data

sealed class TaskResult(
    open val taskId: Int,
    open val processedGraphs: Int,
) {
    data class Invariant(
        override val taskId: Int,
        override val processedGraphs: Int,
        val results: List<GraphResult>,
    ) : TaskResult(taskId, processedGraphs)

    data class Graphs(
        override val taskId: Int,
        override val processedGraphs: Int,
        val graphs: List<String>,
    ) : TaskResult(taskId, processedGraphs)
}