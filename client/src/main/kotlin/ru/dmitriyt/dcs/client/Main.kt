package ru.dmitriyt.dcs.client

import ru.dmitriyt.dcs.client.presentation.ClientApp

fun main(args: Array<String>) {
    val argsManager = ArgsManager(args)
    ClientApp(argsManager).start()
}