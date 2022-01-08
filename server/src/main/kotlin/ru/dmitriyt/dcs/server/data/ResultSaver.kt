package ru.dmitriyt.dcs.server.data

import ru.dmitriyt.dcs.core.data.GraphResult
import java.io.File

/**
 * Сохранение результатов подсчета.
 */
class ResultSaver(solverId: String, n: Int, total: Int) {
    private val resultDirectory = File("result")
    private val file = File("result/${solverId}_${n}_$total.txt")

    fun saveInvariantResult(results: List<GraphResult>) {
        if (!resultDirectory.exists()) {
            resultDirectory.mkdir()
        }
        file.writeText(results.joinToString("\n") { "${it.graph6};${it.invariant}" })
    }

    fun saveConditionResult(graphs: List<String>) {
        if (!resultDirectory.exists()) {
            resultDirectory.mkdir()
        }
        file.writeText(graphs.joinToString("\n"))
    }
}