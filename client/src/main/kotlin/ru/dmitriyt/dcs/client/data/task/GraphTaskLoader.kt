package ru.dmitriyt.dcs.client.data.task

import ru.dmitriyt.dcs.core.GraphTask
import java.io.File
import kotlin.system.exitProcess

/**
 * Загрузчик классов, имплементирующих решение определенного инварианта
 */
class GraphTaskLoader {

    companion object {
        private const val GRAPH_TASKS_DIRECTORY = "tasks"
    }

    /**
     * @param classpath класса, имплеметирующего решение определенного инварианта
     */
    fun loadGraphTask(classpath: String): GraphTask {
        val graphTaskClass = ExtensionLoader<GraphTask>().loadClass(
            directory = "${File.separator}$GRAPH_TASKS_DIRECTORY",
            classpath = classpath,
            parentClass = GraphTask::class.java,
        )
        if (graphTaskClass == null) {
            // не нашли класс
            // идем на сервер TODO
            exitProcess(1)
        } else {
            return graphTaskClass
        }
    }
}