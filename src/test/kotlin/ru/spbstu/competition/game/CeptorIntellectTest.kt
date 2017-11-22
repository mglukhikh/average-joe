package ru.spbstu.competition.game

import org.junit.Assert.*
import org.junit.Test
import ru.spbstu.competition.protocol.data.River
import ru.spbstu.competition.protocol.data.Setup
import ru.spbstu.competition.protocol.data.Site
import ru.spbstu.competition.protocol.data.Map as Graph

class CeptorIntellectTest {

    private fun buildRivers(neighbors: List<List<Int>>): Set<River> {
        val result = mutableSetOf<River>()
        for ((current, currentNeighbors) in neighbors.withIndex()) {
            for (neighbor in currentNeighbors) {
                result += River(current, neighbor)
            }
        }
        return result
    }

    private fun buildGraph(neighbors: List<List<Int>>, mines: List<Int>): Graph {
        return Graph(
                sites = (0..neighbors.size).map { Site(it, null, null) },
                mines = mines,
                rivers = buildRivers(neighbors).toList())
    }

    private fun setupForGraph(neighbors: List<List<Int>>, mines: List<Int>): Setup {
        return Setup(0, 2, buildGraph(neighbors, mines), null)
    }

    @Test
    fun calcMoveSimple() {
        // 0      4
        //   1  3
        // 2      5
        val setup = setupForGraph(listOf(listOf(1), listOf(0, 2, 3), listOf(1), listOf(1, 4, 5), listOf(3), listOf(3)),
                listOf(0, 2, 4, 5))
        val state = State()
        state.init(setup)
        val intellect = CeptorIntellect(state)
        assertEquals(River(1, 3), intellect.calcMove())
    }

}