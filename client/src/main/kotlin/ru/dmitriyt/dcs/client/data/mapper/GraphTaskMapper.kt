package ru.dmitriyt.dcs.client.data.mapper

import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.proto.GraphTaskProto

object GraphTaskMapper {

    fun fromApiToModel(task: GraphTaskProto.Task): Task {
        return Task(
            id = task.id,
            graphs = task.graphsList,
        )
    }

    fun fromModelToApi(result: TaskResult): GraphTaskProto.TaskResult {
        return GraphTaskProto.TaskResult.newBuilder()
            .setTaskId(result.taskId)
            .setProcessedGraphs(result.processedGraphs)
            .apply {
                when (result) {
                    is TaskResult.Graphs -> {
                        resultCondition = GraphTaskProto.TaskConditionResult.newBuilder()
                            .addAllGraphs(result.graphs)
                            .build()
                    }
                    is TaskResult.Invariant -> {
                        resultInvariant = GraphTaskProto.TaskInvariantResult.newBuilder()
                            .addAllResults(result.results.map { it.invariant })
                            .build()
                    }
                }
            }
            .build()
    }
}