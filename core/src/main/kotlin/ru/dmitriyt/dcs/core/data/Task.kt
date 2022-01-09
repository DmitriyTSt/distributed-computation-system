package ru.dmitriyt.dcs.core.data

data class Task(
    val id: Int,
    val partNumber: Int = 0,
    val partsCount: Int = 0,
    val n: Int = 0,
    val args: String = "",
    val isSpecialEmpty: Boolean = false,
    /** Для Standalone режима */
    val graphs: List<String> = emptyList(),
) {
    companion object {
        val EMPTY = Task(0, 0, 0, 0, "", true, emptyList())
    }

    override fun toString(): String {
        return "Task(id=$id, partNumber=$partNumber, n=$n, graphs=$graphs)"
    }
}