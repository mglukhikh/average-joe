package ru.spbstu.competition.game

import org.junit.Assert
import org.junit.Test

class CeptorBlitzIntellectTest : AbstractCeptorTest() {
    @Test
    fun calcMoveSubway() {
        val setup = generateSubwaySetup()
        val stateCeptor = State()
        stateCeptor.init(setup)
        val ceptor = CeptorBlitzIntellect(stateCeptor)
        val stateJoe = State()
        stateJoe.init(setup)
        val joe = JoeIntellect(stateJoe)

        var current: Intellect = joe
        while (stateCeptor.rivers.values.any { it == RiverState.Neutral }) {
            val river = current.calcMove()!!
            if (current != ceptor) {
                println("Turn made by ${current.name}: $river")
            }
            Assert.assertEquals(RiverState.Neutral, stateCeptor.rivers[river])
            Assert.assertEquals(RiverState.Neutral, stateJoe.rivers[river])
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
        Assert.assertEquals(ceptorScore, stateJoe.calcScore { it == RiverState.Enemy })
        val joeScore = stateJoe.calcScore { it == RiverState.Our }
        println("Score for joe: $joeScore")
        Assert.assertEquals(joeScore, stateCeptor.calcScore { it == RiverState.Enemy })
        Assert.assertTrue(ceptorScore > joeScore)
    }

    @Test
    fun calcMoveTriangle() {
        doTestOnGivenJson("maps/triangle.json", CeptorBlitzIntellect::class)
    }

    @Test
    fun calcMoveBoston() {
        doTestOnGivenJson("maps/boston.json", CeptorBlitzIntellect::class)
    }

}