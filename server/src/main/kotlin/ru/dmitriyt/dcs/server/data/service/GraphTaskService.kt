package ru.dmitriyt.dcs.server.data.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.dmitriyt.dcs.core.data.DefaultConfig
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.proto.GraphTaskGrpcKt
import ru.dmitriyt.dcs.proto.GraphTaskProto
import ru.dmitriyt.dcs.server.data.mapper.GraphTaskMapper
import ru.dmitriyt.dcs.server.logd
import ru.dmitriyt.dcs.server.loge
import ru.dmitriyt.dcs.server.presentation.GraphUtils
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class GraphTaskService(
    private val n: Int,
    private val generatorArgs: String,
    private val needSaving: Boolean,
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
        val graphs = listOf("")
        try {
            return if (taskId.get() >= DefaultConfig.GENG_PARTS_COUNT) {
                currentTasksMutex.withLock {
                    logd("get task send task id ${tasks.firstOrNull()?.task?.id}")
                    tasks.firstOrNull()
                        ?: run {
                            onGraphEmpty()
                            logd("get task send empty")
                            buildTaskResponse(Task.EMPTY)
                        }
                }
            } else {
                startTaskHandler(graphs.size)
                val localTaskId = taskId.getAndIncrement()
                val response = buildTaskResponse(Task(localTaskId, localTaskId, n, generatorArgs))
                currentTasksMutex.withLock {
                    tasks.add(response)
                }
                logd("get task send task id ${response.task.id}")
                response
            }
        } catch (e: Exception) {
            loge(e)
            return buildTaskResponse(Task.EMPTY)
        }
    }

    override suspend fun sendTaskResult(
        request: GraphTaskProto.SendTaskResultRequest
    ): GraphTaskProto.SendTaskResultResponse {
        val taskId = request.taskResult.taskId
        currentTasksMutex.withLock {
            tasks.find { it.task.id == taskId }?.let {
                tasks.remove(it)
                val taskResult = GraphTaskMapper.fromApiToModel(
                    request.taskResult,
                    if (needSaving) GraphUtils.getGraphs(it.task) else emptyList()
                )
                // любое завершение запускаем в отдельном скоуп, чтобы выполнился ретурн
                CoroutineScope(Dispatchers.Unconfined).launch {
                    endTaskHandler(taskResult, tasks.size)
                }
            } ?: run {
                logd("task processed yet")
            }
        }
        return GraphTaskProto.SendTaskResultResponse.newBuilder().build()
    }

    private fun buildTaskResponse(task: Task): GraphTaskProto.GetTaskResponse {
        return GraphTaskProto.GetTaskResponse.newBuilder()
            .setTask(GraphTaskMapper.fromModelToApi(task))
            .build()
    }
}