package ru.spbstu.competition.game

import java.util.*

fun State.evaluation(): Int {
    val ourCurrentScore = calcScore { it == RiverState.Our }
    val ourMaximumScore = calcScore { it != RiverState.Enemy }
    val enemyMaximumScore = calcScore { it != RiverState.Our }
    return ourCurrentScore + ourMaximumScore - enemyMaximumScore / 2
}

private fun State.calcScore(includeRivers: (RiverState) -> Boolean): Int {
    var result = 0
    for (mine in mines) {
        result += calcScoreForMine(mine, includeRivers)
    }
    return result
}

private fun State.calcScoreForMine(mine: Int, includeRivers: (RiverState) -> Boolean): Int {
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
            result += getScore(State.Path(mine, neighbor))
        }
    }
    return result
}