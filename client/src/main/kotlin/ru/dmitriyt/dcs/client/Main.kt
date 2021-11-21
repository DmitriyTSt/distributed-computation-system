package ru.dmitriyt.dcs.client

import ru.dmitriyt.dcs.client.presentation.ClientApp
import ru.dmitriyt.dcs.client.presentation.ClientStandaloneApp
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.streams.toList

fun main(args: Array<String>) {
//    var allCount = 0
//    repeat(100) {
//        val process = ProcessBuilder("./geng", "10", "${it}/100", "-u")
//            .directory(File(System.getProperty("user.dir"))).start()
//
//        val reader = BufferedReader(
//            InputStreamReader(
//                process.errorStream
//            )
//        )
//        val lineWithSize = reader.lines().toList()[1]
//        println(lineWithSize)
//        val count = lineWithSize.replace(">Z ", "").let { it.substring(0, it.indexOf(" ")) }.toInt()
//        allCount += count
//    }
//    println("All count for 10 = $allCount")
    val argsManager = ArgsManager(args)
    argsManager.solverId?.let { solverId ->
        ClientStandaloneApp(argsManager).start(solverId)
    } ?: run {
        ClientApp(argsManager).start()
    }
}