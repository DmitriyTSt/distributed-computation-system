package ru.dmitriyt.dcs.client.data.task

import ru.dmitriyt.dcs.client.data.repository.SolverLoaderRepository
import ru.dmitriyt.dcs.core.GraphTask
import java.io.File

/**
 * Загрузчик классов, имплементирующих решение определенного инварианта
 */
class GraphTaskLoader(address: String, port: Int) {

    val solverLoaderRepository = SolverLoaderRepository(address, port)

    companion object {
        const val GRAPH_TASKS_DIRECTORY = "tasks"
    }

    /**
     * @param solverId classpath класса, имплеметирующего решение определенного инварианта
     */
    suspend fun loadGraphTask(solverId: String): GraphTask {
        val extLoader = ExtensionLoader<GraphTask>()
        val graphTaskClass = extLoader.loadClass(
            directory = "${File.separator}$GRAPH_TASKS_DIRECTORY",
            classpath = solverId,
            parentClass = GraphTask::class.java,
        )
        if (graphTaskClass == null) {
            // не нашли класс
            // идем на сервер
            val jar = solverLoaderRepository.getTaskSolver(solverId)
            return extLoader.loadClass(jar, solverId, GraphTask::class.java) ?: run {
                throw Exception("Graph task solver incorrect")
            }
        } else {
            return graphTaskClass
        }
    }
}