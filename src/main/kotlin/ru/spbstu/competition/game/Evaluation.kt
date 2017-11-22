package ru.spbstu.competition.game

import java.util.*

fun State.evaluation(): Int {
    val ourCurrentScore = calcScore { it == RiverState.Our }
    val enemyMaximumScore = calcScore { it != RiverState.Our }
    return ourCurrentScore - enemyMaximumScore / 8
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