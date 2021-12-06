package ru.dmitriyt.dcs.server.data.mapper

import ru.dmitriyt.dcs.core.data.GraphResult
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.proto.GraphTaskProto

object GraphTaskMapper {
    fun fromModelToApi(task: Task): GraphTaskProto.Task {
        return GraphTaskProto.Task.newBuilder()
            .setId(task.id)
            .setPartNumber(task.partNumber)
            .setN(task.n)
            .setArgs(task.args)
            .setIsSpecialEmpty(task.isSpecialEmpty)
            .build()
    }

    fun fromApiToModel(taskResult: GraphTaskProto.TaskResult, graphs: List<String>): TaskResult {
        return if (taskResult.hasResultCondition()) {
            TaskResult.Graphs(
                taskId = taskResult.taskId,
                processedGraphs = taskResult.processedGraphs,
                graphs = taskResult.resultCondition.graphsList,
            )
        } else {
            TaskResult.Invariant(
                taskId = taskResult.taskId,
                processedGraphs = taskResult.processedGraphs,
                results = taskResult.resultInvariant.resultsList.mapIndexed { index, result ->
                    GraphResult(
                        "",
                        result
                    )
                }
            )
        }
    }
}