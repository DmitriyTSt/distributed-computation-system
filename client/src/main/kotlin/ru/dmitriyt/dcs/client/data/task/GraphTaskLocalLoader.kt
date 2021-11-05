package ru.dmitriyt.dcs.client.data.task

import ru.dmitriyt.dcs.core.GraphTask
import java.io.File

/**
 * Локальный загрузчик классов, имплементирующих решение определенного инварианта
 */
class GraphTaskLocalLoader {

    companion object {
        const val GRAPH_TASKS_DIRECTORY = "tasks"
    }

    /**
     * @param solverId classpath класса, имплеметирующего решение определенного инварианта
     */
    fun loadGraphTask(solverId: String): GraphTask? {
        val extLoader = ExtensionLoader<GraphTask>()
        return extLoader.loadClass(
            directory = "${File.separator}$GRAPH_TASKS_DIRECTORY",
            classpath = solverId,
            parentClass = GraphTask::class.java,
        )
    }
}