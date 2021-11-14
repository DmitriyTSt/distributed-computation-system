package ru.dmitriyt.dcs.client.domain

import ru.dmitriyt.dcs.core.GraphCondition
import ru.dmitriyt.dcs.core.GraphInvariant

sealed class GraphTaskInfo {
    data class Invariant(
        val task: GraphInvariant,
    ) : GraphTaskInfo()

    data class Condition(
        val task: GraphCondition,
    ) : GraphTaskInfo()
}