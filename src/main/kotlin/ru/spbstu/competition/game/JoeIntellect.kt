package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River

class JoeIntellect(val state: State, override val protocol: Protocol? = null) : Intellect {

    override val name = "Joe"

    override fun calcMove(): River? {
        // Joe is like super smart!
        // Da best strategy ever!

        // If there is a free river near a mine, take it!
        val try0 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in state.mines || river.target in state.mines)
        }
        if(try0 != null) return try0.key

        // Look at all our pointsees
        val ourSites = state
                .rivers
                .entries
                .filter { it.value == RiverState.Our }
                .flatMap { listOf(it.key.source, it.key.target) }
                .toSet()

        // If there is a river between two our pointsees, take it!
        val try1 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in ourSites && river.target in ourSites)
        }
        if(try1 != null) return try1.key

        // If there is a river near our pointsee, take it!
        val try2 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in ourSites || river.target in ourSites)
        }
        if(try2 != null) return try2.key

        // Bah, take anything left
        val try3 = state.rivers.entries.find { (_, riverState) ->
            riverState == RiverState.Neutral
        }
        if (try3 != null) return try3.key
        return null
    }
}
