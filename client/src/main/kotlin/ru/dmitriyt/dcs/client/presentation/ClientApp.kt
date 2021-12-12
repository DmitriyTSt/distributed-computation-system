package ru.dmitriyt.dcs.client.presentation

import io.grpc.StatusRuntimeException
import kotlinx.coroutines.runBlocking
import ru.dmitriyt.dcs.client.ArgsManager
import ru.dmitriyt.dcs.client.data.repository.GraphTaskRepository
import ru.dmitriyt.dcs.client.data.repository.SolverRepository
import ru.dmitriyt.dcs.client.data.solver.MultiThreadSolver
import ru.dmitriyt.dcs.client.data.solver.SingleSolver
import ru.dmitriyt.dcs.client.loge
import ru.dmitriyt.dcs.client.logd
import ru.dmitriyt.dcs.core.data.Task
import java.util.concurrent.atomic.AtomicInteger

class ClientApp(private val argsManager: ArgsManager) {
    private val repository = ThreadLocal.withInitial {
        GraphTaskRepository(argsManager.serverAddress, argsManager.port)
    }
    private val completedTaskCount = AtomicInteger(0)
    private val solverRepository = SolverRepository(argsManager.serverAddress, argsManager.port)

    fun start() = runBlocking {
        println("Client onStart")
        println("Connecting to ${argsManager.serverAddress}:${argsManager.port}")
        val graphTask = solverRepository.load()
        logd("init with graphTask = ${graphTask::class.java}")
        val solver = if (argsManager.isMulti) {
            MultiThreadSolver(graphTask)
        } else {
            SingleSolver(graphTask)
        }
        solver.run(inputProvider = {
            try {
                repository.get().getTask()
            } catch (e: StatusRuntimeException) {
                loge(e)
                Task.EMPTY
            }
        }, resultHandler = {
            try {
                logd("try send result taskId = ${it.taskId} processed = ${it.processedGraphs}")
                repository.get().sendResult(it)
                completedTaskCount.getAndIncrement()
            } catch (e: Exception) {
                loge(e)
            }
        }, onFinish = {
            repository.get().close()
        })

        println("client on close")
        println("Solved tasks count : ${completedTaskCount.get()}")
    }
}