package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.Map as Graph
import ru.spbstu.competition.protocol.data.Claim
import ru.spbstu.competition.protocol.data.River
import ru.spbstu.competition.protocol.data.Setup

enum class RiverState{ Our, Enemy, Neutral }

class State {
    lateinit var graph: Graph
    lateinit var mineEntrances: Set<River>
    val rivers = mutableMapOf<River, RiverState>()
    var mines = listOf<Int>()
    var myId = -1

    fun init(setup: Setup) {
        myId = setup.punter
        for(river in setup.map.rivers) {
            rivers[river] = RiverState.Neutral
        }
        for(mine in setup.map.mines) {
            mines += mine
        }
        graph = setup.map
        mineEntrances = rivers.keys.filter { river ->
            river.source in mines || river.target in mines
        }.toSet()
    }

    fun update(claim: Claim) {
        rivers[River(claim.source, claim.target)] = when(claim.punter) {
            myId -> RiverState.Our
            else -> RiverState.Enemy
        }
    }
}
