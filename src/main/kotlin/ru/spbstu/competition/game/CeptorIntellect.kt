package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River
import java.util.*

class CeptorIntellect(val state: State, override val protocol: Protocol? = null) : Intellect {

    override val name = "Ceptor"

    override fun calcMove(): River? {
        val startTime = Calendar.getInstance().timeInMillis

        val candidates = linkedSetOf<River>()
        candidates += state.mineEntrances.filter { state.rivers[it] == RiverState.Neutral }
        candidates += state.findBridges { it != RiverState.Enemy }
                .filter { state.rivers[it] == RiverState.Neutral }

        val ourSites = state
                .rivers
                .entries
                .filter { it.value == RiverState.Our }
                .flatMap { listOf(it.key.source, it.key.target) }
                .toSet()
        val neutralRivers = state.rivers.filter { (_, riverState) -> riverState == RiverState.Neutral }.keys
        candidates += neutralRivers.filter { river ->
            river.source in ourSites && river.target in ourSites
        }
        candidates += neutralRivers.filter { river ->
            river.source in ourSites || river.target in ourSites
        }
        candidates += neutralRivers

        var bestRiver: River? = null
        var bestEvaluation = Int.MIN_VALUE
        var timeCounter = 0
        println("Choosing among ${candidates.size} candidates")
        for (river in candidates) {
            assert(state.rivers[river] == RiverState.Neutral)
            state.rivers[river] = RiverState.Our
            val evaluation = state.evaluation()
            if (evaluation > bestEvaluation) {
                bestRiver = river
                bestEvaluation = evaluation
            }
            state.rivers[river] = RiverState.Neutral
            timeCounter++
            if (timeCounter >= 5) {
                timeCounter = 0
                val currentTime = Calendar.getInstance().timeInMillis
                if (currentTime - startTime > 2000) {
                    // Stop calculation if we wasted more than 2 seconds
                    break
                }
            }
        }
        println("Ceptor chooses $bestRiver with evaluation $bestEvaluation")
        if (bestRiver != null) {
            state.rivers[bestRiver] = RiverState.Our
            println("Current score: ${state.calcScore { it == RiverState.Our }}")
            state.rivers[bestRiver] = RiverState.Neutral
        }
        return bestRiver
    }
}
