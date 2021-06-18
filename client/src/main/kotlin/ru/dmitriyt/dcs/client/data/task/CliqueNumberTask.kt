package ru.dmitriyt.dcs.client.data.task

import ru.dmitriyt.dcs.core.domain.GraphTask
import ru.dmitriyt.dcs.core.presentation.Graph
import kotlin.math.max

class CliqueNumberTask : GraphTask {
    override fun solve(graph6: String): Int {
        return cliqueNumber(Graph(graph6))
    }

    fun cliqueNumber(graph: Graph): Int {
        val n = graph.a.size
        val degs = MutableList(n) { 0 }
        var maxDeg = 0
        repeat(n) { i ->
            repeat(n) { j ->
                if (i != j) {
                    if (graph.a[i][j] == 1) {
                        degs[i]++
                    }
                }
            }
            maxDeg = max(maxDeg, degs[i])
        }
        if (maxDeg == 0) {
            return 1
        }
        for (k in (maxDeg + 1) downTo 3) {
            if (hasClique(graph, n, degs, k)) {
                return k
            }
        }
        return 2
    }

    private fun hasClique(graph: Graph, n: Int, deg: List<Int>, k: Int): Boolean {
        val vertexes = mutableListOf<Int>()
        repeat(n) {
            if (deg[it] >= k - 1) {
                vertexes += it
            }
        }
        val m = vertexes.size
        if (m < k) return false
        val comb = MutableList(k) { it }
        while (true) {
            var noEdge = false
            for (i in 0 until k) {
                for (j in (i + 1) until k) {
                    if (graph.a[vertexes[comb[i]]][vertexes[comb[j]]] == 0) {
                        noEdge = true
                        break
                    }
                }
                if (noEdge) break
            }
            if (!noEdge) return true
            if (!nextComb(comb, m)) break
        }
        return false
    }

    private fun nextComb(a: MutableList<Int>, n: Int): Boolean {
        val k = a.size
        for (i in (k - 1) downTo 0) {
            if (a[i] < n - k + i) {
                a[i]++
                for (j in (i + 1) until k) {
                    a[j] = a[j - 1] + 1
                }
                return true
            }
        }
        return false
    }
}