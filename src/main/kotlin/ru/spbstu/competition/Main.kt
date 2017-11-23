package ru.spbstu.competition

import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import ru.spbstu.competition.game.CeptorBlitzIntellect
import ru.spbstu.competition.game.CeptorIntellect
import ru.spbstu.competition.game.JoeIntellect
import ru.spbstu.competition.game.State
import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.*

object Arguments {
    @Option(name = "-u", usage = "Specify server url")
    var url: String = ""

    @Option(name = "-p", usage = "Specify server port")
    var port: Int = -1

    fun use(args: Array<String>): Arguments =
            CmdLineParser(this).parseArgument(*args).let{ this }
}

private fun Setup.tooComplex(): Boolean {
    return map.rivers.size + map.sites.size > 500
}

fun main(args: Array<String>) {
    Arguments.use(args)

    println("Best wishes from Ceptor the Great!")

    // Протокол обмена с сервером
    val protocol = Protocol(Arguments.url, Arguments.port)
    // Состояние игрового поля
    val gameState = State()
    // Джо очень умный чувак, вот его ум
    //val intellect = JoeIntellect(gameState, protocol)

    val myName = "Ceptor"
    protocol.handShake(myName)
    val setupData = protocol.setup()
    // А уж Цептор-то ну ваще гений...
    val intellect = when {
        setupData.tooComplex() -> CeptorBlitzIntellect(gameState, protocol)
        else -> CeptorIntellect(gameState, protocol)
    }
    println("Chosen intellect: ${intellect.name}")
    gameState.init(setupData)

    println("Received id = ${setupData.punter}")

    protocol.ready()

    gameloop@ while(true) {
        val message = protocol.serverMessage()
        when(message) {
            is GameResult -> {
                println("The game is over!")
                val myScore = message.stop.scores[protocol.myId]
                println("$myName scored ${myScore.score} points!")
                break@gameloop
            }
            is Timeout -> {
                println("$myName too slow =(")
            }
            is GameTurnMessage -> {
                for(move in message.move.moves) {
                    when(move) {
                        is PassMove -> {}
                        is ClaimMove -> gameState.update(move.claim)
                    }
                }
            }
        }

        println("$myName thinkin'")
        intellect.makeMove()
        println("Got it!")
    }
}
