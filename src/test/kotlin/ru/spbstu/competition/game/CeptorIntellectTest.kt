package ru.spbstu.competition.game

import org.junit.Assert.*
import org.junit.Test
import ru.spbstu.competition.protocol.data.River
import ru.spbstu.competition.protocol.data.Setup
import ru.spbstu.competition.protocol.data.Site
import java.io.File
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

    private fun generateSubwaySetup(): Setup {
        val neighbors = listOf(
                listOf(1),
                listOf(0, 2),
                listOf(1, 3),
                listOf(2, 4),
                listOf(3, 5),
                listOf(4, 6),
                listOf(5, 7, 25, 26), // ! 6
                listOf(6, 8, 52, 26), // ! 7
                listOf(7, 9, 48, 26), // ! 8
                listOf(8, 10, 27, 41), // ! 9
                listOf(9, 11),
                listOf(10, 12),
                listOf(11, 13),
                listOf(12, 14),
                listOf(13, 15), // ! 14
                listOf(14, 16),
                listOf(15, 17),
                listOf(16, 18),
                listOf(17), // 18

                listOf(20), // 19
                listOf(19, 21),
                listOf(20, 22),
                listOf(21, 23),
                listOf(22, 24),
                listOf(23, 25), // 24
                listOf(24,  6), // 25
                listOf( 6, 27, 7, 8, 53), // ! 26
                listOf(26, 28, 9, 42), // ! 27
                listOf(27, 29),
                listOf(28, 30),
                listOf(29, 31),
                listOf(30, 32),
                listOf(31, 33),
                listOf(32, 34),
                listOf(33, 35),
                listOf(34), // 35

                listOf(37), // 36
                listOf(36, 38),
                listOf(37, 39),
                listOf(38, 40),
                listOf(39, 41),
                listOf(40,  9, 47, 48), // ! 41
                listOf(27, 43), // 42
                listOf(42), // 43

                listOf(45), // 44
                listOf(44, 46),
                listOf(45, 47),
                listOf(46, 41), // 47
                listOf(41, 8),  // 48

                listOf(50), // 49
                listOf(49, 51),
                listOf(50, 52),
                listOf(51, 7), // 52
                listOf(26, 54), // 53
                listOf(53, 55),
                listOf(54, 56),
                listOf(55, 57),
                listOf(56, 58),
                listOf(57) // 58
        )
        val mines = listOf(6, 7, 8, 9, 14, 26, 27, 41)
        return setupForGraph(neighbors, mines)
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

    private fun String.readNumberList(): List<Int> {
        var startDigit = -1
        var endDigit = -1
        val result = mutableListOf<Int>()
        for ((index, char) in this.withIndex()) {
            if (char.isDigit()) {
                if (startDigit == -1) {
                    startDigit = index
                    endDigit = index
                }
                else {
                    endDigit = index
                }
            }
            else if (startDigit != -1) {
                result += this.substring(startDigit, endDigit + 1).toInt()
                startDigit = -1
                endDigit = -1
            }
        }
        return result
    }

    private fun readGraphFromJsonFile(name: String): Graph {
        val line = File(name).readLines().first()
        val afterLastId = line.substringAfterLast("\"id\":")
        val lastSiteNumber = afterLastId.takeWhile { it.isDigit() }.toInt()
        val beforeMines = afterLastId.substringBefore("\"mines\":")
        val sourceAndTarget = beforeMines.readNumberList()
        val rivers = mutableListOf<River>()
        var isSource = true
        var source = -1
        var target: Int
        for (num in sourceAndTarget) {
            if (isSource) {
                source = num
            }
            else {
                target = num
                rivers += River(source, target)
            }
            isSource = !isSource
        }

        val afterMines = afterLastId.substringAfter("\"mines\":")
        val mines = afterMines.readNumberList()
        return Graph((0..lastSiteNumber).map { Site(it, null, null) }, rivers, mines)
    }

    @Test
    fun calcMoveTriangle() {
        val setup = Setup(0, 2, readGraphFromJsonFile("triangle.json"), null)
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
}