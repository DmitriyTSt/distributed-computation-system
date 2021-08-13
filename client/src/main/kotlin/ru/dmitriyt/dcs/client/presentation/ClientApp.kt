package ru.dmitriyt.dcs.client.presentation

import ru.dmitriyt.dcs.client.ArgsManager
import ru.dmitriyt.dcs.client.data.repository.GraphTaskRepository
import ru.dmitriyt.dcs.client.data.solver.MultiThreadSolver
import ru.dmitriyt.dcs.client.data.solver.SingleSolver
import ru.dmitriyt.dcs.client.data.task.CliqueNumberTask
import java.util.concurrent.atomic.AtomicInteger

class ClientApp(private val argsManager: ArgsManager) {
    private val repository = ThreadLocal.withInitial {
        GraphTaskRepository(argsManager.serverAddress, argsManager.port)
    }
    private val completedTaskCount = AtomicInteger(0)

    fun start() {
        println("Client onStart")
        val graphTask = CliqueNumberTask()
        val solver = if (argsManager.isMulti) {
            MultiThreadSolver(graphTask)
        } else {
            SingleSolver(graphTask)
        }
        solver.run(inputProvider = {
            repository.get().getTask()
        }, resultHandler = {
            repository.get().sendResult(it)
            completedTaskCount.getAndAdd(it.results.size)
        }, onFinish = {
            repository.get().close()
        })

        println("client on close")
        println("Solved tasks count : ${completedTaskCount.get()}")
    }
}