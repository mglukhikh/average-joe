package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River

interface Intellect {
    val protocol: Protocol?
    val name: String
    fun calcMove(): River?
    fun makeMove() {
        val protocol = protocol ?: return
        return calcMove()?.let { protocol.claimMove(it.source, it.target) } ?: protocol.passMove()
    }
}