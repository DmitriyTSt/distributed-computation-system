package ru.dmitriyt.dcs.client.data.repository

import io.grpc.ManagedChannelBuilder
import ru.dmitriyt.dcs.client.data.mapper.GraphTaskMapper
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.proto.GraphTaskGrpc
import ru.dmitriyt.dcs.proto.GraphTaskProto
import java.io.Closeable

class GraphTaskRepository(server: String, port: Int) : Closeable {
    private val channel = ManagedChannelBuilder.forAddress(server, port).usePlaintext().build()
    private val stub = GraphTaskGrpc.newBlockingStub(channel)

    fun getTask(): Task {
        return stub.getTask(
            GraphTaskProto.GetTaskRequest.newBuilder().build()
        ).task.let { GraphTaskMapper.fromApiToModel(it) }
    }

    fun sendResult(taskResult: TaskResult) {
        try {
            stub.sendTaskResult(
                GraphTaskProto.SendTaskResultRequest.newBuilder()
                    .setTaskResult(GraphTaskMapper.fromModelToApi(taskResult))
                    .build()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun close() {
        channel.shutdown()
    }
}