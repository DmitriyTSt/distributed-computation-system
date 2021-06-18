package ru.dmitriyt.dcs.core.presentation

class Graph(
    code: String
) {
    companion object {
        const val MAX_N = 16
    }

    var a: Array<IntArray> = Array(MAX_N) { IntArray(MAX_N) }
    var n: Int = 0

    init {
        if (code.isNotEmpty()) {
            var el: Int
            val n: Int = (code[0] - 63).toInt()
            this.n = n
            var i = 0
            var j = 1
            for (k in 1 until code.length) {
                el = (code[k] - 63).toInt()
                for (p in 5 downTo 0) {
                    a[i][j] = el shr p and 1
                    a[j][i] = a[i][j]
                    i++
                    if (i >= j) {
                        i = 0
                        j++
                    }
                }
            }
        }
    }

    fun inversed(): Graph {
        val graph = Graph("")
        graph.n = this.n
        repeat(n) { i ->
            repeat(n) { j ->
                graph.a[i][j] = (a[i][j] + 1) % 2
            }
        }
        return graph
    }
}