package ru.dmitriyt.dcs.client.presentation

import kotlinx.coroutines.runBlocking
import ru.dmitriyt.dcs.client.ArgsManager
import ru.dmitriyt.dcs.client.data.solver.MultiThreadSolver
import ru.dmitriyt.dcs.client.data.solver.SingleSolver
import ru.dmitriyt.dcs.client.data.LocalResultSaver
import ru.dmitriyt.dcs.client.data.repository.SolverRepository
import ru.dmitriyt.dcs.client.domain.GraphTaskInfo
import ru.dmitriyt.dcs.core.data.Task
import ru.dmitriyt.dcs.core.data.TaskResult
import ru.dmitriyt.dcs.core.presentation.Graph
import ru.dmitriyt.dcs.core.presentation.TimeHelper
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicIntegerArray

class ClientStandaloneApp(private val argsManager: ArgsManager) {

    companion object {
        private const val PART_SIZE = 1000
    }

    private lateinit var graphTaskInfo: GraphTaskInfo
    private val taskResults = mutableListOf<TaskResult>()
    private val taskId = AtomicInteger(0)
    private val processedGraphs = AtomicInteger(0)
    private val solverRepository = SolverRepository(argsManager.serverAddress, argsManager.port)
    private val ansInvariant = AtomicIntegerArray(Graph.MAX_N)
    private val ansCondition = AtomicInteger(0)
    private var total = AtomicInteger(0)
    private var startTime = 0L
    private var endTime = 0L
    private var isCompleted = AtomicBoolean(false)
    private var isFinished = AtomicBoolean(false)
    private val lock = Object()

    fun start(currentSolverId: String) = runBlocking {
        println("Client onStart")
        graphTaskInfo = solverRepository.loadStandalone(currentSolverId)
        val solver = if (argsManager.isMulti) {
            MultiThreadSolver(graphTaskInfo)
        } else {
            SingleSolver(graphTaskInfo)
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
                synchronized(lock) {
                    taskResults.add(taskResult)
                }
            }
        }, onFinish = {
            if (total.get() == processedGraphs.get() && isCompleted.get() && !isFinished.get()) {
                isFinished.set(true)
                endTime = System.currentTimeMillis()
                printResult()
                if (argsManager.needSaving) {
                    val localResultSaver = LocalResultSaver(currentSolverId, total.get())
                    println("Saving results")
                    when (graphTaskInfo) {
                        is GraphTaskInfo.Condition -> {
                            val results = synchronized(lock) {
                                taskResults.flatMap { (it as TaskResult.Graphs).graphs }
                            }
                            localResultSaver.saveConditionResult(results)
                        }
                        is GraphTaskInfo.Invariant -> {
                            val results = synchronized(lock) {
                                taskResults.flatMap { (it as TaskResult.Invariant).results }
                            }
                            localResultSaver.saveInvariantResult(results)
                        }
                    }
                    println("Results saved")
                }
            }
        })

        println("client on close")
        println("Solved graphs count : ${processedGraphs.get()}")
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