package ru.dmitriyt.dcs.server

import ru.dmitriyt.dcs.server.presentation.ServerApp

private var arguments: ArgsManager? = null

fun main(args: Array<String>) {
    val argsManager = ArgsManager(args)
    arguments = argsManager
    ServerApp(argsManager).start()
}

fun logd(text: String) {
    if (arguments?.isDebug == true) {
        println(text)
    }
}

fun loge(e: Exception) {
    if (arguments?.isDebug == true) {
        e.printStackTrace()
    }
}

fun logi(text: String) {
    if (arguments?.isInfo == true) {
        println(text)
    }
}