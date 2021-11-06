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

    fun fromApiToModel(taskResult: GraphTaskProto.TaskResult, graphs: List<String>): TaskResult {
        return TaskResult(
            taskId = taskResult.taskId,
            results = taskResult.resultsList.mapIndexed { index, result -> GraphResult(graphs[index], result) }
        )
    }
}