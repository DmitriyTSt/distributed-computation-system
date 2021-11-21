package ru.dmitriyt.dcs.core.data

data class Task(
    val id: Int,
    val partNumber: Int = 0,
    val n: Int = 0,
    /** Для Standalone режима */
    val graphs: List<String> = emptyList(),
) {
    companion object {
        val EMPTY = Task(0, 0, 0, emptyList())
    }

    override fun toString(): String {
        return "Task(id=$id, partNumber=$partNumber, n=$n, graphs=$graphs)"
    }
}