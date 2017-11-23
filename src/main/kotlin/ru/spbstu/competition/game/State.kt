package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.Map as Graph
import ru.spbstu.competition.protocol.data.Claim
import ru.spbstu.competition.protocol.data.River
import ru.spbstu.competition.protocol.data.Setup
import java.util.*

enum class RiverState{ Our, Enemy, Neutral }

class State {
    lateinit var graph: Graph
    lateinit var mineEntrances: Set<River>
    lateinit var adjacentRivers: Map<Int, List<River>>

    data class Path(val mine: Int, val site: Int)
    private val scoreCache = mutableMapOf<Path, Int>()

    val rivers = mutableMapOf<River, RiverState>()
    var mines = listOf<Int>()
    var myId = -1

    fun getScore(path: Path): Int = scoreCache[path]!!

    fun River.otherSide(site: Int) = if (source == site) target else source

    fun neighbors(site: Int): Sequence<Int> =
            (adjacentRivers[site] ?: emptyList()).asSequence().map { it.otherSide(site) }

    private fun bfs(mine: Int) {
        val visited = mutableMapOf<Int, Int>()
        val queue = ArrayDeque<Int>()
        visited[mine] = 0
        queue += mine
        scoreCache[Path(mine, mine)] = 0
        while (queue.isNotEmpty()) {
            val current = queue.poll()
            val distance = visited[current]!!
            for (neighbor in neighbors(current)) {
                if (neighbor in visited) continue
                visited[neighbor] = distance + 1
                scoreCache[Path(mine, neighbor)] = (distance + 1) * (distance + 1)
                queue += neighbor
            }
        }
    }

    fun initCache() {
        if (scoreCache.isNotEmpty()) return
        for (mine in mines) {
            bfs(mine)
        }
    }

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
        val adjacentRivers = mutableMapOf<Int, MutableList<River>>()
        for (river in rivers.keys) {
            adjacentRivers.getOrPut(river.source) { mutableListOf() }.add(river)
            adjacentRivers.getOrPut(river.target) { mutableListOf() }.add(river)
        }
        this.adjacentRivers = adjacentRivers
    }

    fun update(claim: Claim) {
        rivers[River(claim.source, claim.target)] = when(claim.punter) {
            myId -> RiverState.Our
            else -> RiverState.Enemy
        }
    }
}
