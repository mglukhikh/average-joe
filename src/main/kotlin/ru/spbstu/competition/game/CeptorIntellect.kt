package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River
import java.util.*

class CeptorIntellect(val state: State, override val protocol: Protocol? = null) : Intellect {

    override val name = "Ceptor"

    private fun generateCandidates(): Collection<River> {
        val freeEntrances = state.mineEntrances.filter { state.rivers[it] == RiverState.Neutral }
        val freeBridges = state.findBridges { it != RiverState.Enemy }
                .filter { state.rivers[it] == RiverState.Neutral }
        println("Total bridges: ${freeBridges.size}")
        val candidates = linkedSetOf<River>()
        candidates += freeEntrances
        candidates += freeBridges

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
        if (candidates.isNotEmpty()) return candidates
        candidates += neutralRivers.filter { river ->
            river.source in ourSites || river.target in ourSites
        }
        if (candidates.isNotEmpty()) return candidates
        return neutralRivers
    }

    override fun calcMove(): River? {
        val startTime = Calendar.getInstance().timeInMillis

        val candidates = generateCandidates()
        var bestRiver: River? = null
        var bestEvaluation = Int.MIN_VALUE
        println("Choosing among ${candidates.size} candidates")
        for ((candidateIndex, river) in candidates.withIndex()) {
            val evaluation = state.evaluation(riverToTake = river)
            if (evaluation > bestEvaluation) {
                bestRiver = river
                bestEvaluation = evaluation
            }
            val currentTime = Calendar.getInstance().timeInMillis
            val timePassed = currentTime - startTime
            if (timePassed > 2000 || timePassed > 1300 && candidateIndex == 0) {
                // Stop calculation if we wasted more than 2 seconds
                println("Break by time after considering ${candidateIndex + 1} candidates")
                break
            }
        }
        println("Ceptor chooses $bestRiver with evaluation $bestEvaluation")
        val endTime = Calendar.getInstance().timeInMillis
        println("Time to choose: ${endTime - startTime}")
        if (bestRiver != null) {
            state.rivers[bestRiver] = RiverState.Our
            println("Current score: ${state.calcScore { it == RiverState.Our }}")
            state.rivers[bestRiver] = RiverState.Neutral
        }
        return bestRiver
    }
}
