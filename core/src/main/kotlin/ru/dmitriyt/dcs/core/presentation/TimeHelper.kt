package ru.dmitriyt.dcs.core.presentation

object TimeHelper {
    fun getFormattedSpentTime(startTime: Long, endTime: Long): String {
        val totalTime = endTime - startTime
        val millis = totalTime % 1000
        val seconds = totalTime / 1000 % 60
        val minutes = totalTime / 1000 / 60 % 60
        val hours = totalTime / 1000 / 60 / 60
        val timeBuilder = StringBuilder()
        if (hours != 0L) timeBuilder.append("${hours}h ")
        if (minutes != 0L || timeBuilder.isNotEmpty()) timeBuilder.append("${minutes}m ")
        timeBuilder.append("%d.%03ds".format(seconds, millis))
        return timeBuilder.toString()
    }
}