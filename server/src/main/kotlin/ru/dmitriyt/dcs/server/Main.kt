package ru.dmitriyt.dcs.server

import ru.dmitriyt.dcs.server.presentation.ServerApp

fun main(args: Array<String>) {
    val argsManager = ArgsManager(args)
    ServerApp(argsManager).start()
}