package ru.dmitriyt.dcs.client.data.repository

import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import ru.dmitriyt.dcs.client.domain.GraphTaskInfo
import ru.dmitriyt.dcs.core.GraphCondition
import ru.dmitriyt.dcs.core.GraphInvariant
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
    val solverClassLoader = SolverClassLoader()

    /**
     * Загружает локальный класс задачи
     */
    fun loadStandalone(solverId: String): GraphTaskInfo {
        val graphInvariant = solverClassLoader.load<GraphInvariant>(solverId)
        val graphCondition = solverClassLoader.load<GraphCondition>(solverId)
        return when {
            graphInvariant != null -> GraphTaskInfo.Invariant(graphInvariant)
            graphCondition != null -> GraphTaskInfo.Condition(graphCondition)
            else -> {
                System.err.println("Local graph task not found")
                exitProcess(1)
            }
        }
    }

    /**
     * Загружает класс локально, или, если не найден, скачивает с сервера
     */
    suspend fun load(): GraphTaskInfo {
        val solverId = getCurrentSolverId()
        val graphInvariant = solverClassLoader.load<GraphInvariant>(solverId)
        val graphCondition = solverClassLoader.load<GraphCondition>(solverId)
        return when {
            graphInvariant != null -> GraphTaskInfo.Invariant(graphInvariant)
            graphCondition != null -> GraphTaskInfo.Condition(graphCondition)
            else -> {
                // если нет локально, пробуем загрузить с сервера
                getTaskSolver(solverId)
                val graphInvariantAfterLoad = solverClassLoader.load<GraphInvariant>(solverId)
                val graphConditionAfterLoad = solverClassLoader.load<GraphCondition>(solverId)
                when {
                    graphInvariantAfterLoad != null -> GraphTaskInfo.Invariant(graphInvariantAfterLoad)
                    graphConditionAfterLoad != null -> GraphTaskInfo.Condition(graphConditionAfterLoad)
                    else -> {
                        System.err.println("Error graph task load")
                        exitProcess(1)
                    }
                }
            }
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