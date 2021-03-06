package ru.dmitriyt.dcs.server

import ru.dmitriyt.dcs.core.data.DefaultConfig
import kotlin.system.exitProcess

class ArgsManager(_args: Array<String>) {
    private val args = _args.toList()

    val isDebug = args.contains("-d")
    val isInfo = args.contains("-i")
    val solverId = getParam("-j")
    val port = getParam("--port")?.toIntOrNull() ?: DefaultConfig.DEFAULT_PORT
    val generatorArgs = getParam("--args").orEmpty()
    val needSaving = args.contains("-s")
    val partsCount = getParam("-p")?.toIntOrNull() ?: DefaultConfig.DEFAULT_PARTS_COUNT

    val n = getParam("-n")?.toIntOrNull() ?: run {
        System.err.println("-n is required")
        exitProcess(1)
    }

    private fun getParam(key: String): String? {
        return args.indexOf(key).takeIf { it > -1 }?.let { args.getOrNull(it + 1) }
    }
}