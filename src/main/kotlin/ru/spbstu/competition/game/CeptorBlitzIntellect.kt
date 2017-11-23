package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River

class CeptorBlitzIntellect(val state: State, override val protocol: Protocol? = null) : Intellect {

    override val name = "Ceptor Blitz"

    override fun calcMove(): River? {
        val freeEntrances = state.mineEntrances.filter { state.rivers[it] == RiverState.Neutral }
        val freeBridges = state.findBridges { it != RiverState.Enemy }
                .filter { state.rivers[it] == RiverState.Neutral }
        val freeBridgesToEntrances = freeBridges.intersect(freeEntrances)
        if (freeBridgesToEntrances.isNotEmpty()) return freeBridgesToEntrances.first()

        val ourSites = state
                .rivers
                .entries
                .filter { it.value == RiverState.Our }
                .flatMap { listOf(it.key.source, it.key.target) }
                .toSet()
        val freeRivers = state.rivers.filter { (_, riverState) -> riverState == RiverState.Neutral }.keys
        val freeConnectingRivers = freeRivers.filter { river ->
            river.source in ourSites && river.target in ourSites
        }
        val freeConnectingRiversToEntrances = freeConnectingRivers.intersect(freeEntrances)
        if (freeConnectingRiversToEntrances.isNotEmpty()) return freeConnectingRiversToEntrances.first()
        val freeConnectingBridges = freeConnectingRivers.intersect(freeBridges)
        if (freeConnectingBridges.isNotEmpty()) return freeConnectingBridges.first()

        if (freeEntrances.isNotEmpty()) return freeEntrances.first()

        val freeAdjacentRivers = freeRivers.filter { river ->
            river.source in ourSites || river.target in ourSites
        }
        val freeAdjacentBridges = freeAdjacentRivers.intersect(freeBridges)
        if (freeAdjacentBridges.isNotEmpty()) return freeAdjacentBridges.first()

        if (freeBridges.isNotEmpty()) return freeBridges.first()
        if (freeConnectingRivers.isNotEmpty()) return freeConnectingRivers.first()
        if (freeAdjacentRivers.isNotEmpty()) return freeAdjacentRivers.first()

        return freeRivers.firstOrNull()
    }
}
