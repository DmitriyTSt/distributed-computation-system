package ru.dmitriyt.dcs.server

import ru.dmitriyt.dcs.core.data.DefaultConfig

class ArgsManager(_args: Array<String>) {
    private val args = _args.toList()

    val isDebug = args.contains("-d")
    val solverId = getParam("-j")
    val port = getParam("--port")?.toIntOrNull() ?: DefaultConfig.DEFAULT_PORT
    val partSize = getParam("-p")?.toIntOrNull() ?: 1000

    private fun getParam(key: String): String? {
        return args.indexOf(key).takeIf { it > -1 }?.let { args.getOrNull(it + 1) }
    }
}