package ru.dmitriyt.dcs.server.presentation

import io.grpc.ServerBuilder
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.core.presentation.Graph
import ru.dmitriyt.dcs.server.ArgsManager
import ru.dmitriyt.dcs.server.data.service.GraphTaskService
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicIntegerArray
import kotlin.system.exitProcess

class ServerApp(private val argsManager: ArgsManager) {
    private val ans = AtomicIntegerArray(Graph.MAX_N)
    private var total = AtomicInteger(0)
    private var startTime = 0L
    private var endTime = 0L
    private var processedGraphs = AtomicInteger(0)
    private var resultHandled = false
    private val resultMutex = Mutex()
    private var isCompleted = false

    private val server = ServerBuilder
        .forPort(argsManager.port)
        .addService(GraphTaskService(argsManager.partSize, ::handleStart, ::handleResult) { isCompleted = true })
        .build()

    fun start() {
        server.start()
        println("Server started at port ${argsManager.port}")
        Runtime.getRuntime().addShutdownHook(
            Thread {
                this.server.shutdown()
            }
        )
        server.awaitTermination()
    }

    private fun handleStart(partSize: Int) {
        if (startTime == 0L) {
            startTime = System.currentTimeMillis()
        }
        total.getAndAdd(partSize)
    }

    private suspend fun handleResult(taskResult: TaskResult, tasksInProgress: Int) {
        processedGraphs.getAndAdd(taskResult.results.size)

        taskResult.results.forEach {
            ans.getAndIncrement(it.invariant)
        }
//        println("total = ${this.total.get()}, processed = ${processedGraphs.get()}, inprogress = $tasksInProgress, isComplete = $isCompleted")
        if (this.total.get() == processedGraphs.get() && tasksInProgress == 0 && isCompleted) {
            resultMutex.withLock {
                if (!resultHandled) {
                    resultHandled = true
                    endTime = System.currentTimeMillis()
                    printResultAndStop()
                }
            }
        }
    }

    private fun printResultAndStop() {
        println("Total: ${total.get()}")
        val simpleAns = mutableListOf<Int>()
        repeat(Graph.MAX_N) {
            simpleAns.add(ans.get(it))
        }
        simpleAns.forEachIndexed { index, count ->
            println("$index: $count")
        }
        println(TimeHelper.getFormattedSpentTime(startTime, endTime))
        exitProcess(0)
    }
}