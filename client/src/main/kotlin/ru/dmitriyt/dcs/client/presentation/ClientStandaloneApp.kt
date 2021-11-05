package ru.dmitriyt.dcs.client.presentation

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import ru.dmitriyt.dcs.client.ArgsManager
import ru.dmitriyt.dcs.client.data.solver.MultiThreadSolver
import ru.dmitriyt.dcs.client.data.solver.SingleSolver
import ru.dmitriyt.dcs.client.data.task.GraphTaskLoader
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.presentation.Graph
import ru.dmitriyt.dcs.core.presentation.TimeHelper
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicIntegerArray

class ClientStandaloneApp(private val argsManager: ArgsManager) {

    companion object {
        private const val PART_SIZE = 1000
    }

    private val taskId = AtomicInteger(0)
    private val processedGraphs = AtomicInteger(0)
    private val graphTaskLoader = GraphTaskLoader(argsManager.serverAddress, argsManager.port)
    private val ans = AtomicIntegerArray(Graph.MAX_N)
    private var total = AtomicInteger(0)
    private var startTime = 0L
    private var endTime = 0L
    private var isCompleted = AtomicBoolean(false)
    private var isFinished = AtomicBoolean(false)

    fun start(currentSolverId: String) = runBlocking {
        println("Client onStart")
        val graphTask = graphTaskLoader.loadGraphTask(currentSolverId)
        val solver = if (argsManager.isMulti) {
            MultiThreadSolver(graphTask)
        } else {
            SingleSolver(graphTask)
        }
        startTime = System.currentTimeMillis()
        solver.run(inputProvider = {
            val graphs = mutableListOf<String>()
            repeat(PART_SIZE) {
                readLine()?.let { graphs.add(it) } ?: run {
                    return@repeat
                }
            }
            if (graphs.isEmpty()) {
                isCompleted.set(true)
            }
            total.getAndAdd(graphs.size)
            taskId.getAndIncrement()
            Task(
                id = taskId.get(),
                graphs = graphs,
            )
        }, resultHandler = { taskResult ->
            processedGraphs.getAndAdd(taskResult.results.size)

            taskResult.results.forEach {
                ans.getAndIncrement(it.invariant)
            }
        }, onFinish = {
            if (total.get() == processedGraphs.get() && isCompleted.get() && !isFinished.get()) {
                isFinished.set(true)
                endTime = System.currentTimeMillis()
                printResult()
            }
        })

        println("client on close")
        println("Solved graphs count : ${processedGraphs.get()}")
    }

    private fun printResult() {
        println("Total: ${total.get()}")
        val simpleAns = mutableListOf<Int>()
        repeat(Graph.MAX_N) {
            simpleAns.add(ans.get(it))
        }
        simpleAns.forEachIndexed { index, count ->
            println("$index: $count")
        }
        println(TimeHelper.getFormattedSpentTime(startTime, endTime))
    }
}