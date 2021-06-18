package ru.dmitriyt.dcs.server.data.mapper

import ru.dmitriyt.dcs.core.data.GraphResult
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.proto.GraphTaskProto

object GraphTaskMapper {
    fun fromModelToApi(task: Task): GraphTaskProto.Task {
        return GraphTaskProto.Task.newBuilder()
            .setId(task.id)
            .addAllGraphs(task.graphs)
            .build()
    }

    fun fromApiToModel(taskResult: GraphTaskProto.TaskResult): TaskResult {
        return TaskResult(
            taskId = taskResult.taskId,
            results = taskResult.resultsList.map { fromApiToModel(it) }
        )
    }

    private fun fromApiToModel(graphResult: GraphTaskProto.GraphResult): GraphResult {
        return GraphResult(
            graph6 = graphResult.graph,
            invariant = graphResult.invariant,
        )
    }
}