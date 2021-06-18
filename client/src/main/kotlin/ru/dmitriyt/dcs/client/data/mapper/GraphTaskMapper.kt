package ru.dmitriyt.dcs.client.data.mapper

import ru.dmitriyt.dcs.core.data.GraphResult
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.proto.GraphTaskProto

object GraphTaskMapper {

    fun fromApiToModel(task: GraphTaskProto.Task): Task {
        return Task(
            id = task.id,
            graphs = task.graphsList
        )
    }

    fun fromModelToApi(result: TaskResult): GraphTaskProto.TaskResult {
        return GraphTaskProto.TaskResult.newBuilder()
            .setTaskId(result.taskId)
            .addAllResults(result.results.map { fromModelToApi(it) })
            .build()
    }

    private fun fromModelToApi(result: GraphResult): GraphTaskProto.GraphResult {
        return GraphTaskProto.GraphResult.newBuilder()
            .setGraph(result.graph6)
            .setInvariant(result.invariant)
            .build()
    }
}