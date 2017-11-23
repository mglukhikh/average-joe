package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.River
import java.util.*

fun State.evaluation(riverToTake: River): Int {
    assert(rivers[riverToTake] == RiverState.Neutral)
    // Now we can have ourMaximumScoreWithourRiver, enemy can have enemyMaximumScore
    // If enemy claim this river, we can have ourMaximumScoreWithoutRiver < ourMaximumScoreWithourRiver
    // so we lose ourMaximumScoreWithourRiver - ourMaximumScoreWithoutRiver (more is better for us)
    // If we claim this river, enemy can have enemyMaximumScoreWithoutRiver < enemyMaximumScore,
    // so he loses enemyMaximumScore - enemyMaximumScoreWithoutRiver (more is better for us)
    // Total value is below, and first part is constant
    // (ourMaximumScore + enemyMaximumScore) - (ourMaximumScoreWithoutRiver + enemyMaximumScoreWithoutRiver)
    rivers[riverToTake] = RiverState.Enemy
    val ourMaximumScoreWithoutRiver = calcScore { it != RiverState.Enemy }
    rivers[riverToTake] = RiverState.Our
    val enemyMaximumScoreWithoutRiver = calcScore { it != RiverState.Our }
    rivers[riverToTake] = RiverState.Neutral
    return -(ourMaximumScoreWithoutRiver + enemyMaximumScoreWithoutRiver)
}

fun State.calcScore(log: Boolean = false, includeRivers: (RiverState) -> Boolean): Int {
    var result = 0
    for (mine in mines) {
        result += calcScoreForMine(mine, log, includeRivers)
    }
    return result
}

private fun State.calcScoreForMine(mine: Int, log: Boolean, includeRivers: (RiverState) -> Boolean): Int {
    val visited = mutableSetOf<Int>()
    val queue = ArrayDeque<Int>()
    visited += mine
    queue += mine
    var result = 0
    while (queue.isNotEmpty()) {
        val current = queue.poll()
        for (river in adjacentRivers[current] ?: emptyList()) {
            if (!includeRivers(rivers[river]!!)) continue
            val neighbor = river.otherSide(current)
            if (neighbor in visited) continue
            visited += neighbor
            queue += neighbor
            val path = State.Path(mine, neighbor)
            val score = getScore(path)
            if (log) {
                println("Score for $path: $score")
            }
            result += score
        }
    }
    return result
}