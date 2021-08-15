package ru.dmitriyt.dcs.server.data.service

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.proto.GraphTaskGrpcKt
import ru.dmitriyt.dcs.proto.GraphTaskProto
import ru.dmitriyt.dcs.server.data.mapper.GraphTaskMapper
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class GraphTaskService(
    private val isDebug: Boolean,
    private val partSize: Int = 1000,
    private val startTaskHandler: (Int) -> Unit,
    private val endTaskHandler: suspend (result: TaskResult, taskInProgress: Int) -> Unit,
    private val onGraphEmpty: () -> Unit,
) : GraphTaskGrpcKt.GraphTaskCoroutineImplBase() {
    private val taskId = AtomicInteger(0)
    private val tasks = LinkedList<GraphTaskProto.GetTaskResponse>()
    private val currentTasksMutex = Mutex()

    override suspend fun getTask(
        request: GraphTaskProto.GetTaskRequest
    ): GraphTaskProto.GetTaskResponse {
        val graphs = mutableListOf<String>()
        repeat(partSize) {
            readLine()?.let { graphs.add(it) } ?: run {
                return@repeat
            }
        }
        try {
            return if (graphs.isEmpty()) {
                currentTasksMutex.withLock {
                    tasks.firstOrNull() ?: run {
                        onGraphEmpty()
                        buildTaskResponse(Task.EMPTY)
                    }
                }
            } else {
                if (graphs.size != partSize) {
                    onGraphEmpty()
                }
                startTaskHandler(graphs.size)
                val localTaskId = taskId.getAndIncrement()
                val response = buildTaskResponse(Task(localTaskId, graphs))
                currentTasksMutex.withLock {
                    tasks.add(response)
                }
                response
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return buildTaskResponse(Task.EMPTY)
        }
    }

    override suspend fun sendTaskResult(
        request: GraphTaskProto.SendTaskResultRequest
    ): GraphTaskProto.SendTaskResultResponse {
        val taskResult = GraphTaskMapper.fromApiToModel(request.taskResult)
        currentTasksMutex.withLock {
            tasks.find { it.task.id == taskResult.taskId }?.let {
                tasks.remove(it)
                endTaskHandler(taskResult, tasks.size)
            } ?: run {
                if (isDebug) {
                    println("task processed yet")
                }
            }
        }
        try {
            return GraphTaskProto.SendTaskResultResponse.newBuilder().build()
        } catch (e: Exception) {
            e.printStackTrace()
            return GraphTaskProto.SendTaskResultResponse.newBuilder().build()
        }
    }

    private fun buildTaskResponse(task: Task): GraphTaskProto.GetTaskResponse {
        return GraphTaskProto.GetTaskResponse.newBuilder()
            .setTask(GraphTaskMapper.fromModelToApi(task))
            .build()
    }
}