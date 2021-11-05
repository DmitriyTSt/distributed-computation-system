package ru.dmitriyt.dcs.client.presentation

import kotlinx.coroutines.runBlocking
import ru.dmitriyt.dcs.client.ArgsManager
import ru.dmitriyt.dcs.client.data.repository.GraphTaskRepository
import ru.dmitriyt.dcs.client.data.solver.MultiThreadSolver
import ru.dmitriyt.dcs.client.data.solver.SingleSolver
import ru.dmitriyt.dcs.client.data.task.GraphTaskLoader
import java.util.concurrent.atomic.AtomicInteger

class ClientApp(private val argsManager: ArgsManager) {
    private val repository = ThreadLocal.withInitial {
        GraphTaskRepository(argsManager.serverAddress, argsManager.port)
    }
    private val completedTaskCount = AtomicInteger(0)
    private val graphTaskLoader = GraphTaskLoader(argsManager.serverAddress, argsManager.port)

    fun start() = runBlocking {
        println("Client onStart")
        println("Connecting to ${argsManager.serverAddress}:${argsManager.port}")
        val currentSolverId = graphTaskLoader.solverLoaderRepository.getCurrentSolverId()
        val graphTask = graphTaskLoader.loadGraphTask(currentSolverId)
        val solver = if (argsManager.isMulti) {
            MultiThreadSolver(graphTask)
        } else {
            SingleSolver(graphTask)
        }
        solver.run(inputProvider = {
            repository.get().getTask()
        }, resultHandler = {
            try {
                repository.get().sendResult(it)
                completedTaskCount.getAndAdd(it.results.size)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, onFinish = {
            repository.get().close()
        })

        println("client on close")
        println("Solved tasks count : ${completedTaskCount.get()}")
    }
}