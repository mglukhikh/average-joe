package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.Map as Graph
import ru.spbstu.competition.protocol.data.River
import ru.spbstu.competition.protocol.data.Site

fun State.findBridges(includeRivers: (RiverState) -> Boolean): Set<River> =
        BridgeTraverser(this, includeRivers).findBridges()

private class BridgeTraverser(val state: State, val includeRivers: (RiverState) -> Boolean) {
    val graph = state.graph
    val visitedVertices = mutableSetOf<Int>()

    var traverseIndex = 0

    class Info(var inIndex: Int, var loopIndex: Int) {
        constructor(index: Int): this(index, index)
    }

    val verticesInfo = mutableMapOf<Int, Info>()

    val bridges = mutableSetOf<River>()

    fun findBridges(): Set<River> {
        for (vertex in graph.sites) {
            if (vertex.id in visitedVertices) continue
            traverse(vertex, null)
        }
        return bridges
    }

    fun traverse(current: Site, previous: Site?) =
            traverse(current.id, previous?.id)

    fun traverse(currentId: Int, previousId: Int?) {
        visitedVertices += currentId
        verticesInfo[currentId] = Info(traverseIndex++)
        loop@ for ((river, riverState) in state.rivers) {
            if (!includeRivers(riverState)) continue@loop
            val nextId = when (currentId) {
                river.source -> river.target
                river.target -> river.source
                else -> continue@loop
            }
            if (nextId == previousId) continue
            val currentInfo = verticesInfo[currentId]!!
            if (nextId in visitedVertices) {
                val nextInfo = verticesInfo[nextId]!!
                currentInfo.loopIndex = minOf(currentInfo.loopIndex, nextInfo.inIndex)
            }
            else {
                traverse(nextId, currentId)
                val nextInfo = verticesInfo[nextId]!!
                currentInfo.loopIndex = minOf(currentInfo.loopIndex, nextInfo.loopIndex)
                if (nextInfo.loopIndex > currentInfo.inIndex) {
                    bridges += river
                }
            }
        }
    }
}
