package ru.dmitriyt.dcs.client.data.repository

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import ru.dmitriyt.dcs.core.GraphTask
import ru.dmitriyt.dcs.core.data.classloader.SolverClassLoader
import ru.dmitriyt.dcs.proto.SolverLoaderGrpcKt
import ru.dmitriyt.dcs.proto.SolverLoaderProto
import java.io.Closeable
import java.io.File
import kotlin.system.exitProcess

class SolverRepository(address: String, port: Int) : Closeable {
    private val channel = ManagedChannelBuilder.forAddress(address, port).usePlaintext().build()
    private val stub = SolverLoaderGrpcKt.SolverLoaderCoroutineStub(channel)

    /** Загрузчик локальных классов */
    private val solverClassLoader = SolverClassLoader()

    /**
     * Загрузка текущего класса решения локально или с сервера
     * @param _solverId - classpath решения, если запускаем локально
     */
    suspend fun loadGraphTask(_solverId: String? = null): GraphTask {
        val solverId = _solverId ?: getCurrentSolverId()
        val graphTaskClass = solverClassLoader.load<GraphTask>(solverId)
        return if (graphTaskClass == null) {
            if (_solverId != null) {
                System.err.println("Local graph task not found")
            }
            // не нашли класс
            // идем на сервер
            try {
                getTaskSolver(solverId)
            } catch (e: Exception) {
                System.err.println("Load graph task from server error")
                exitProcess(1)
            }
            solverClassLoader.load<GraphTask>(solverId) ?: run {
                throw Exception("Graph task solver incorrect")
            }
        } else {
            graphTaskClass
        }
    }

    private suspend fun getTaskSolver(solverId: String): File {
        val dir = File(SolverClassLoader.GRAPH_TASKS_DIRECTORY)
        if (!dir.exists()) {
            dir.mkdir()
        }
        val file = File("${SolverClassLoader.GRAPH_TASKS_DIRECTORY}/$solverId.jar")
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

    private suspend fun getCurrentSolverId(): String {
        return stub.getCurrentSolverId(SolverLoaderProto.GetCurrentSolverIdRequest.newBuilder().build()).solverId
    }

    override fun close() {
        channel.shutdown()
    }
}