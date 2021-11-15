package ru.dmitriyt.dcs.core.data.classloader

import java.io.File

/**
 * Локальный загрузчик классов решения
 */
class SolverClassLoader {

    companion object {
        const val GRAPH_TASKS_DIRECTORY = "tasks"
    }

    /**
     * @param solverId classpath класса решения
     */
    inline fun <reified T> load(solverId: String): T? {
        val extLoader = ExtensionLoader<T>()
        return extLoader.loadClass(
            directory = "${File.separator}$GRAPH_TASKS_DIRECTORY",
            classpath = solverId,
            parentClass = T::class.java,
        )
    }
}