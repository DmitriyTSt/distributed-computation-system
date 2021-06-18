package ru.dmitriyt.dcs.server.data.service

import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.proto.GraphTaskGrpcKt
import ru.dmitriyt.dcs.proto.GraphTaskProto
import ru.dmitriyt.dcs.server.data.mapper.GraphTaskMapper
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class GraphTaskService(
    private val partSize: Int = 1000,
    private val startTaskHandler: (Int) -> Unit,
    private val endTaskHandler: suspend (result: TaskResult, taskInProgress: Int) -> Unit,
    private val onGraphEmpty: () -> Unit,
) : GraphTaskGrpcKt.GraphTaskCoroutineImplBase() {
    private val taskId = AtomicInteger(0)
    private val tasks = LinkedList<GraphTaskProto.GetTaskResponse>()

    override suspend fun getTask(
        request: GraphTaskProto.GetTaskRequest
    ): GraphTaskProto.GetTaskResponse {
        val graphs = mutableListOf<String>()
        repeat(partSize) {
            readLine()?.let { graphs.add(it) } ?: run {
                return@repeat
            }
        }
        return if (graphs.isEmpty()) {
            tasks.firstOrNull() ?: run {
                onGraphEmpty()
                buildTaskResponse(Task.EMPTY)
            }
        } else {
            if (graphs.size != partSize) {
                onGraphEmpty()
            }
            startTaskHandler(graphs.size)
            val localTaskId = taskId.getAndIncrement()
            val response = buildTaskResponse(Task(localTaskId, graphs))
            tasks.add(response)
            response
        }
    }

    override suspend fun sendTaskResult(
        request: GraphTaskProto.SendTaskResultRequest
    ): GraphTaskProto.SendTaskResultResponse {
        val taskResult = GraphTaskMapper.fromApiToModel(request.taskResult)
        tasks.find { it.task.id == taskResult.taskId }?.let {
            tasks.remove(it)
//            println("receive task $taskId with total = ${request.total}")
            endTaskHandler(taskResult, tasks.size)
        }
        return GraphTaskProto.SendTaskResultResponse.newBuilder().build()
    }

    private fun buildTaskResponse(task: Task): GraphTaskProto.GetTaskResponse {
        return GraphTaskProto.GetTaskResponse.newBuilder()
            .setTask(GraphTaskMapper.fromModelToApi(task))
            .build()
    }
}