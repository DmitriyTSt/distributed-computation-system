package ru.dmitriyt.dcs.client

import ru.dmitriyt.dcs.client.presentation.ClientApp
import ru.dmitriyt.dcs.client.presentation.ClientStandaloneApp

fun main(args: Array<String>) {
    val argsManager = ArgsManager(args)
    argsManager.solverId?.let { solverId ->
        ClientStandaloneApp(argsManager).start(solverId)
    } ?: run {
        ClientApp(argsManager).start()
    }
}