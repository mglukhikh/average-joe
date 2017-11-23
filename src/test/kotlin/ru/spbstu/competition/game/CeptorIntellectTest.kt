package ru.spbstu.competition.game

import org.junit.Assert.*
import org.junit.Test
import ru.spbstu.competition.protocol.data.River
import ru.spbstu.competition.protocol.data.Setup
import ru.spbstu.competition.protocol.data.Map as Graph

class CeptorIntellectTest : AbstractCeptorTest() {

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
        val river = intellect.calcMove()
        assertEquals(state.rivers[river!!], RiverState.Neutral)
        assertEquals(River(1, 3), river)
        state.rivers[river] = RiverState.Our
        state.rivers[River(0, 1)] = RiverState.Enemy
        val river2 = intellect.calcMove()
        assertEquals(state.rivers[river2!!], RiverState.Neutral)
        state.rivers[river2] = RiverState.Our
        state.rivers[River(3, 5)] = RiverState.Enemy
        val river3 = intellect.calcMove()
        assertEquals(state.rivers[river3], RiverState.Neutral)
    }

    @Test
    fun calcMoveEnemyWins() {
        // 0 (e) 1 (e) 2 (e) 3 (e) 4 5 (e) 6 (e) 7 (e) 8 (e) 9
        val setup = setupForGraph(listOf(
                listOf(1),
                listOf(0, 2),
                listOf(1, 3),
                listOf(2, 4),
                listOf(3, 5),
                listOf(4, 6),
                listOf(5, 7),
                listOf(6, 8),
                listOf(7, 9),
                listOf(8)
        ), listOf(0, 9))
        val state = State()
        state.init(setup)
        state.rivers[River(0, 1)] = RiverState.Enemy
        state.rivers[River(1, 2)] = RiverState.Enemy
        state.rivers[River(2, 3)] = RiverState.Enemy
        state.rivers[River(3, 4)] = RiverState.Enemy
        state.rivers[River(5, 6)] = RiverState.Enemy
        state.rivers[River(6, 7)] = RiverState.Enemy
        state.rivers[River(7, 8)] = RiverState.Enemy
        state.rivers[River(8, 9)] = RiverState.Enemy
        val intellect = CeptorIntellect(state)
        val river = intellect.calcMove()
        assertEquals(state.rivers[river!!], RiverState.Neutral)
        assertEquals(River(4, 5), river)
    }

    @Test
    fun calcMoveSubway() {
        val setup = generateSubwaySetup()
        val stateCeptor = State()
        stateCeptor.init(setup)
        val ceptor = CeptorIntellect(stateCeptor)
        val stateJoe = State()
        stateJoe.init(setup)
        val joe = JoeIntellect(stateJoe)

        var current: Intellect = joe
        while (stateCeptor.rivers.values.any { it == RiverState.Neutral }) {
            val river = current.calcMove()!!
            if (current != ceptor) {
                println("Turn made by ${current.name}: $river")
            }
            assertEquals(RiverState.Neutral, stateCeptor.rivers[river])
            assertEquals(RiverState.Neutral, stateJoe.rivers[river])
            when (current) {
                ceptor -> {
                    current = joe
                    stateCeptor.rivers[river] = RiverState.Our
                    stateJoe.rivers[river] = RiverState.Enemy
                }
                joe -> {
                    stateCeptor.rivers[river] = RiverState.Enemy
                    stateJoe.rivers[river] = RiverState.Our
                    current = ceptor
                }
            }
        }
        val ceptorScore = stateCeptor.calcScore(log = true) { it == RiverState.Our }
        println("Score for ceptor: $ceptorScore")
        assertEquals(ceptorScore, stateJoe.calcScore { it == RiverState.Enemy })
        val joeScore = stateJoe.calcScore { it == RiverState.Our }
        println("Score for joe: $joeScore")
        assertEquals(joeScore, stateCeptor.calcScore { it == RiverState.Enemy })
        assertTrue(ceptorScore > joeScore)
    }


    @Test
    fun noBridgesInTriangle() {
        val setup = Setup(0, 2, readGraphFromJsonFile("maps/triangle.json"), null)
        val stateCeptor = State()
        stateCeptor.init(setup)
        assertEquals(emptyList<River>(), stateCeptor.findBridges { true }.toList())
    }

    @Test
    fun calcMoveTriangle() {
        doTestOnGivenJson("maps/triangle.json", CeptorIntellect::class)
    }
}