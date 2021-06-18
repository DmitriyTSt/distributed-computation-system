package ru.dmitriyt.dcs.core.data

data class Task(
    val id: Int,
    val graphs: List<String>
) {
    companion object {
        val EMPTY = Task(0, emptyList())
    }
}