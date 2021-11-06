package ru.dmitriyt.dcs.client.data

import ru.dmitriyt.dcs.core.data.GraphResult
import java.io.File

/**
 * Класс для сохранения результатов подсчета
 */
class LocalResultSaver(solverId: String, total: Int) {
    private val resultDirectory = File("result")
    private val file = File("result/${solverId}_$total.txt")

    fun saveResult(results: List<GraphResult>) {
        if (!resultDirectory.exists()) {
            resultDirectory.mkdir()
        }
        file.writeText(results.joinToString("\n") { "${it.graph6};${it.invariant}" })
    }
}