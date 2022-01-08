package ru.dmitriyt.dcs.client

import ru.dmitriyt.dcs.client.presentation.ClientApp
import ru.dmitriyt.dcs.client.presentation.ClientStandaloneApp

private var arguments: ArgsManager? = null

fun main(args: Array<String>) {
    val argsManager = ArgsManager(args)
    arguments = argsManager
    argsManager.solverId?.let { solverId ->
        ClientStandaloneApp(argsManager).start(solverId)
    } ?: run {
        ClientApp(argsManager).start()
    }
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