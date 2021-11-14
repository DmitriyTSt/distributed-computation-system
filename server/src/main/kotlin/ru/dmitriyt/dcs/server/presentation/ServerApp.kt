package ru.dmitriyt.dcs.server.presentation

import io.grpc.ServerBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.dmitriyt.dcs.core.GraphCondition
import ru.dmitriyt.dcs.core.GraphInvariant
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.core.data.classloader.SolverClassLoader
import ru.dmitriyt.dcs.core.presentation.Graph
import ru.dmitriyt.dcs.core.presentation.TimeHelper
import ru.dmitriyt.dcs.server.ArgsManager
import ru.dmitriyt.dcs.server.data.ResultSaver
import ru.dmitriyt.dcs.server.data.service.GraphTaskService
import ru.dmitriyt.dcs.server.data.service.SolverLoaderService
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicIntegerArray
import kotlin.system.exitProcess

class ServerApp(private val argsManager: ArgsManager) {
    private val ansInvariant = AtomicIntegerArray(Graph.MAX_N)
    private val ansCondition = AtomicInteger(0)
    private var total = AtomicInteger(0)
    private var startTime = 0L
    private var endTime = 0L
    private var processedGraphs = AtomicInteger(0)
    private var resultHandled = false
    private val finishMutex = Mutex()
    private var isCompleted = false
    private val taskResults = mutableListOf<TaskResult>()
    private val resultMutex = Mutex()

    private val solverClassLoader = SolverClassLoader()

    private val server = ServerBuilder
        .forPort(argsManager.port)
        .addService(
            GraphTaskService(
                isDebug = argsManager.isDebug,
                partSize = argsManager.partSize,
                startTaskHandler = ::handleStart,
                endTaskHandler = ::handleResult,
                onGraphEmpty = { isCompleted = true },
            )
        )
        .addService(SolverLoaderService(solverId = argsManager.solverId.orEmpty()))
        .build()

    fun start() {
        if (argsManager.solverId.isNullOrEmpty()) {
            System.err.println("-j task solver classpath is required")
            exitProcess(1)
        }
        server.start()
        println("Server started at port ${argsManager.port}")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                server.shutdown()
            }
        )

        // проверка корректности поданной задачи (инвариант или проверка на условие)
        val solver = solverClassLoader.load<GraphInvariant>(argsManager.solverId)
            ?: solverClassLoader.load<GraphCondition>(argsManager.solverId)
        if (solver == null) {
            System.err.println(
                "jar with ${argsManager.solverId} classpath not found in ${SolverClassLoader.GRAPH_TASKS_DIRECTORY} directory"
            )
            exitProcess(1)
        }

        server.awaitTermination()
    }

    private fun handleStart(partSize: Int) {
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
        }
        total.getAndAdd(partSize)
    }

    private suspend fun handleResult(taskResult: TaskResult, tasksInProgress: Int) {
        processedGraphs.getAndAdd(taskResult.processedGraphs)

        when (taskResult) {
            is TaskResult.Graphs -> {
                ansCondition.getAndAdd(taskResult.graphs.size)
            }
            is TaskResult.Invariant -> {
                taskResult.results.forEach {
                    ansInvariant.getAndIncrement(it.invariant)
                }
            }
        }

        if (argsManager.needSaving) {
            resultMutex.withLock {
                taskResults.add(taskResult)
            }
        }

        if (argsManager.isDebug) {
            println(
                "total = %d, processed = %d, inProgress = %d, isCompleted = %s".format(
                    total.get(),
                    processedGraphs.get(),
                    tasksInProgress,
                    isCompleted.toString(),
                )
            )
        }
        if (this.total.get() <= processedGraphs.get() && tasksInProgress == 0 && isCompleted) {
            finishMutex.withLock {
                if (!resultHandled) {
                    resultHandled = true
                    endTime = System.currentTimeMillis()
                    printResult()
                    if (argsManager.needSaving) {
                        val localResultSaver = ResultSaver(argsManager.solverId.orEmpty(), total.get())
                        println("Saving results")
                        when (taskResults.firstOrNull()) {
                            is TaskResult.Graphs -> {
                                localResultSaver.saveConditionResult(
                                    taskResults.flatMap { (it as TaskResult.Graphs).graphs }
                                )
                            }
                            is TaskResult.Invariant -> {
                                localResultSaver.saveInvariantResult(
                                    taskResults.flatMap { (it as TaskResult.Invariant).results }
                                )
                            }
                            else -> Unit
                        }
                        println("Results saved")
                    }
                    // ожидаем последние запросы клиента, чтобы у него не сыпались ошибки
                    delay(2000)
                    server.shutdown()
                }
            }
        }
    }

    private fun printResult() {
        println("Total: ${total.get()}")
        val simpleAns = mutableListOf<Int>()
        repeat(Graph.MAX_N) {
            simpleAns.add(ansInvariant.get(it))
        }
        simpleAns.forEachIndexed { index, count ->
            println("$index: $count")
        }
        println(TimeHelper.getFormattedSpentTime(startTime, endTime))
    }
}