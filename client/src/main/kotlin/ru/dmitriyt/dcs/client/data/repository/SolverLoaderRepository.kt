package ru.dmitriyt.dcs.client.data.repository

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import ru.dmitriyt.dcs.client.data.task.GraphTaskLoader
import ru.dmitriyt.dcs.proto.SolverLoaderGrpcKt
import ru.dmitriyt.dcs.proto.SolverLoaderProto
import java.io.Closeable
import java.io.File

class SolverLoaderRepository(address: String, port: Int) : Closeable {
    private val channel = ManagedChannelBuilder.forAddress(address, port).usePlaintext().build()
    private val stub = SolverLoaderGrpcKt.SolverLoaderCoroutineStub(channel)

    suspend fun getTaskSolver(solverId: String): File {
        val dir = File(GraphTaskLoader.GRAPH_TASKS_DIRECTORY)
        if (!dir.exists()) {
            dir.mkdir()
        }
        val file = File("${GraphTaskLoader.GRAPH_TASKS_DIRECTORY}/$solverId.jar")
        withContext(Dispatchers.IO) {
            file.createNewFile()
        }
        stub.getSolver(
            SolverLoaderProto.GetSolverRequest.newBuilder().setSolverId(solverId).build()
        ).collect { response ->
            withContext(Dispatchers.IO) {
                file.appendBytes(response.data.toByteArray())
            }
        }
        return file
    }

    suspend fun getCurrentSolverId(): String {
        return stub.getCurrentSolverId(SolverLoaderProto.GetCurrentSolverIdRequest.newBuilder().build()).solverId
    }

    override fun close() {
        channel.shutdown()
    }
}