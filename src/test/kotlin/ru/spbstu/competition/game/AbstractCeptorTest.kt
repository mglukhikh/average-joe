package ru.spbstu.competition.game

import org.junit.Assert
import ru.spbstu.competition.protocol.data.Map as Graph
import ru.spbstu.competition.protocol.data.River
import ru.spbstu.competition.protocol.data.Setup
import ru.spbstu.competition.protocol.data.Site
import java.io.File
import kotlin.reflect.KClass

abstract class AbstractCeptorTest {
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

    protected fun setupForGraph(neighbors: List<List<Int>>, mines: List<Int>): Setup {
        return Setup(0, 2, buildGraph(neighbors, mines), null)
    }

    protected fun generateSubwaySetup(): Setup {
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

    protected fun readGraphFromJsonFile(name: String): Graph {
        val line = File(name).readLines().first()
        val afterLastId = line.substringAfterLast("\"id\":")
        val lastSiteNumber = afterLastId.takeWhile { it.isDigit() }.toInt()
        val beforeMines = afterLastId.substringAfter("\"rivers\":").substringBefore("\"mines\":")
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

    protected fun doTestOnGivenJson(fileName: String, intellectClass: KClass<out Intellect>) {
        val setup = Setup(0, 2, readGraphFromJsonFile(fileName), null)
        val stateOur = State()
        stateOur.init(setup)
        val ourIntellect = intellectClass.constructors.first().call(stateOur, null)
        val stateJoe = State()
        stateJoe.init(setup)
        val joe = JoeIntellect(stateJoe)

        var current: Intellect = joe
        while (stateOur.rivers.values.any { it == RiverState.Neutral }) {
            val river = current.calcMove()!!
            if (current != ourIntellect) {
                println("Turn made by ${current.name}: $river")
            }
            Assert.assertEquals(RiverState.Neutral, stateOur.rivers[river])
            Assert.assertEquals(RiverState.Neutral, stateJoe.rivers[river])
            when (current) {
                ourIntellect -> {
                    current = joe
                    stateOur.rivers[river] = RiverState.Our
                    stateJoe.rivers[river] = RiverState.Enemy
                }
                joe -> {
                    stateOur.rivers[river] = RiverState.Enemy
                    stateJoe.rivers[river] = RiverState.Our
                    current = ourIntellect
                }
            }
        }
        stateOur.initCache()
        stateJoe.initCache()
        val ceptorScore = stateOur.calcScore(log = true) { it == RiverState.Our }
        println("Score for ${ourIntellect.name}: $ceptorScore")
        Assert.assertEquals(ceptorScore, stateJoe.calcScore { it == RiverState.Enemy })
        val joeScore = stateJoe.calcScore { it == RiverState.Our }
        println("Score for joe: $joeScore")
        Assert.assertEquals(joeScore, stateOur.calcScore { it == RiverState.Enemy })
        Assert.assertTrue(ceptorScore > joeScore)
    }
}