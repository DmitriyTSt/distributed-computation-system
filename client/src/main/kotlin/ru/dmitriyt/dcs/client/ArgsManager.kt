package ru.dmitriyt.dcs.client

import ru.dmitriyt.dcs.core.data.DefaultConfig

class ArgsManager(_args: Array<String>) {
    private val args = _args.toList()

    val isMulti = args.contains("-m")
    val serverAddress = getParam("--server") ?: DefaultConfig.DEFAULT_SERVER
    val port = getParam("--port")?.toIntOrNull() ?: DefaultConfig.DEFAULT_PORT

    private fun getParam(key: String): String? {
        return args.indexOf(key).takeIf { it > -1 }?.let { args.getOrNull(it + 1) }
    }
}