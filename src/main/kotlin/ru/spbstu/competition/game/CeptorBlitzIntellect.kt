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
        if (freeBridgesToEntrances.isNotEmpty()) {
            println("Blitz takes free bridge to entrance: ${freeBridgesToEntrances.first()}")
            return freeBridgesToEntrances.first()
        }

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
        if (freeConnectingRiversToEntrances.isNotEmpty()) {
            println("Blitz takes free connecting river to entrance: ${freeConnectingRiversToEntrances.first()}")
            return freeConnectingRiversToEntrances.first()
        }
        val freeConnectingBridges = freeConnectingRivers.intersect(freeBridges)
        if (freeConnectingBridges.isNotEmpty()) {
            println("Blitz takes free connecting bridge: ${freeConnectingBridges.first()}")
            return freeConnectingBridges.first()
        }

        if (freeEntrances.isNotEmpty()) {
            println("Blitz takes free entrance: ${freeEntrances.first()}")
            return freeEntrances.first()
        }

        val freeAdjacentRivers = freeRivers.filter { river ->
            river.source in ourSites || river.target in ourSites
        }
        val freeAdjacentBridges = freeAdjacentRivers.intersect(freeBridges)
        if (freeAdjacentBridges.isNotEmpty()) {
            println("Blitz takes free adjacent bridge: ${freeAdjacentBridges.first()}")
            return freeAdjacentBridges.first()
        }

        if (freeBridges.isNotEmpty()) {
            println("Blitz takes free bridge: ${freeBridges.first()}")
            return freeBridges.first()
        }
        if (freeConnectingRivers.isNotEmpty()) {
            println("Blitz takes free connecting river: ${freeConnectingRivers.first()}")
            return freeConnectingRivers.first()
        }
        if (freeAdjacentRivers.isNotEmpty()) {
            println("Blitz takes free adjacent river: ${freeAdjacentRivers.first()}")
            return freeAdjacentRivers.first()
        }

        if (freeRivers.isNotEmpty()) {
            println("Blitz takes just free river: ${freeRivers.first()}")
        }

        println("Nothing to take for Blitz")
        return null
    }
}
