package com.example.mühleServer

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.Lob
import kotlin.math.abs

@Entity
internal class Game {
    @Id
    @GeneratedValue
    var id: Long = 0
    var currentPlayer: Player
    var state: State
    private var previousState: State
    private var movedFieldR = 0
    private var movedFieldN = 0
    var turn = 0

    @Lob
    var board: Array<Array<Field>>

    init {
        currentPlayer = Player.P1
        board = Array(3) { Array(8) {Field.EMPTY} }
        state = State.EARLYGAME
        previousState = state
    }

    private fun getFieldCount(field: Field): Int {
        var result = 0
        for (i in board.indices) {
            for (j in board[i].indices) {
                if (board[i][j] == field) {
                    result++
                }
            }
        }
        return result
    }

    fun checkMühle(ring: Int, field: Int): Boolean {
        if (board[ring][field] == Field.EMPTY) {
            return false
        }
        when (field) {
            0 -> {
                if ((board[ring][field] == board[ring][field + 1] &&
                                board[ring][field] == board[ring][field + 2] || board[ring][0] == board[ring][7]) && board[ring][0] == board[ring][6]) {
                    return true
                }
            }

            2, 4 -> {
                if (board[ring][field] == board[ring][field + 1] &&
                        board[ring][field] == board[ring][field + 2] || board[ring][field] == board[ring][field - 1] && board[ring][field] == board[ring][field - 2]) {
                    return true
                }
            }

            6 -> {
                if (board[ring][field] == board[ring][field - 1] &&
                        board[ring][field] == board[ring][field - 2] || board[ring][0] == board[ring][7] && board[ring][0] == board[ring][6]) {
                    return true
                }
            }

            1, 3, 5 -> {
                if ((board[0][field] == board[1][field] &&
                                board[0][field] == board[2][field] || board[ring][field] == board[ring][field + 1]) && board[ring][field] == board[ring][field - 1]) {
                    return true
                }
            }

            7 -> {
                if (board[0][field] == board[1][field] &&
                        board[0][field] == board[2][field] || board[ring][field] == board[ring][0] && board[ring][field] == board[ring][field - 1]) {
                    return true
                }
            }
        }
        return false
    }

    fun checkOnlyMühle(player: Player): Boolean {
        for (i in board.indices) {
            for (j in board[i].indices) {
                when (player) {
                    Player.P1 -> {
                        if (board[i][j] == Field.P1) {
                            if (!checkMühle(i, j)) {
                                return false
                            }
                        }
                    }

                    Player.P2 -> {
                        if (board[i][j] == Field.P2) {
                            if (!checkMühle(i, j)) {
                                return false
                            }
                        }
                    }
                }
            }
        }
        return true
    }

    fun playTurn(selectedFieldR: Int, selectedFieldN: Int) {
        if ((0 > selectedFieldN || selectedFieldN > 7) && (0 > selectedFieldR || selectedFieldR > 2)) {
            throw IllegalMoveException(state, selectedFieldR, selectedFieldN)
        }
        when (state) {
            State.EARLYGAME -> {
                if (board[selectedFieldR][selectedFieldN] == Field.EMPTY) {
                    when (currentPlayer) {
                        Player.P1 -> {
                            board[selectedFieldR][selectedFieldN] = Field.P1
                        }

                        Player.P2 -> {
                            board[selectedFieldR][selectedFieldN] = Field.P2
                        }
                    }
                    turn++
                    if (turn == 18) {
                        state = State.LATEGAME
                    }
                    checkEnd()
                    if (checkMühle(selectedFieldR, selectedFieldN)) {
                        previousState = state
                        state = State.STEALING
                        return
                    }
                    switchCurrentPlayer()
                    checkEnd()
                } else {
                    throw IllegalMoveException(state, selectedFieldR, selectedFieldN)
                }
                return
            }

            State.LATEGAME -> {
                previousState = State.LATEGAME
                when (currentPlayer) {
                    Player.P1 -> {
                        if (board[selectedFieldR][selectedFieldN] != Field.P1) {
                            throw IllegalMoveException(state, selectedFieldR, selectedFieldN)
                        }
                    }

                    Player.P2 -> {
                        if (board[selectedFieldR][selectedFieldN] != Field.P2) {
                            throw IllegalMoveException(state, selectedFieldR, selectedFieldN)
                        }
                    }
                }
                board[selectedFieldR][selectedFieldN] = Field.MOVED
                movedFieldR = selectedFieldR
                movedFieldN = selectedFieldN
                state = State.MOVING
                return
            }

            State.STEALING -> {
                when (currentPlayer) {
                    Player.P1 -> {
                        if (board[selectedFieldR][selectedFieldN] != Field.P2 || checkMühle(selectedFieldR, selectedFieldN) && !checkOnlyMühle(Player.P2)) {
                            throw IllegalMoveException(state, selectedFieldR, selectedFieldN)
                        }
                    }

                    Player.P2 -> {
                        if (board[selectedFieldR][selectedFieldN] != Field.P1 || checkMühle(selectedFieldR, selectedFieldN) && !checkOnlyMühle(Player.P1)) {
                            throw IllegalMoveException(state, selectedFieldR, selectedFieldN)
                        }
                    }
                }
                board[selectedFieldR][selectedFieldN] = Field.EMPTY
                state = previousState
                switchCurrentPlayer()
                checkEnd()
                return
            }

            State.MOVING -> {
                if (board[selectedFieldR][selectedFieldN] == Field.MOVED) {
                    when (currentPlayer) {
                        Player.P1 -> {
                            board[selectedFieldR][selectedFieldN] = Field.P1
                        }

                        Player.P2 -> {
                            board[selectedFieldR][selectedFieldN] = Field.P2
                        }
                    }
                    movedFieldN = -5
                    movedFieldR = -5
                    state = State.LATEGAME
                    return
                }
                if (board[selectedFieldR][selectedFieldN] != Field.EMPTY) {
                    throw IllegalMoveException(state, selectedFieldR, selectedFieldN)
                }
                var jump = false
                when (currentPlayer) {
                    Player.P1 -> {
                        if (getFieldCount(Field.P1) < 3) {
                            jump = true
                        }
                    }

                    Player.P2 -> {
                        if (getFieldCount(Field.P2) < 3) {
                            jump = true
                        }
                    }
                }
                if (((abs(selectedFieldR - movedFieldR) == 1 && selectedFieldN == movedFieldN &&
                                selectedFieldN%2 == 1) ||
                                (abs(selectedFieldN - movedFieldN) == 1 && selectedFieldR == movedFieldR) ||
                                (selectedFieldN == 0 && movedFieldN == 7 && selectedFieldR == movedFieldR) ||
                                (selectedFieldN == 7 && movedFieldN == 0 && selectedFieldR == movedFieldR)) ||
                        jump
                ) {
                    when (currentPlayer) {
                        Player.P1 -> {
                            board[selectedFieldR][selectedFieldN] = Field.P1
                        }

                        Player.P2 -> {
                            board[selectedFieldR][selectedFieldN] = Field.P2
                        }
                    }
                    board[movedFieldR][movedFieldN] = Field.EMPTY
                    movedFieldN = -5
                    movedFieldR = -5
                    state = State.LATEGAME
                    turn++
                    if (checkMühle(selectedFieldR, selectedFieldN)) {
                        state = State.STEALING
                    } else {
                        switchCurrentPlayer()
                        checkEnd()
                    }
                }
            }
            State.DRAW, State.P1WIN, State.P2WIN -> {return}
        }
    }

    fun checkEnd(): Boolean {
        if (state == State.MOVING) {
            return false
        }
        if (turn <= 18) {
            if (getFieldCount(Field.P1) + (17 - turn) / 2 < 3) {
                state = State.P2WIN
                return true
            }
            if (getFieldCount(Field.P2) + (18 - turn) / 2 < 3) {
                state = State.P1WIN
                return true
            }
        } else {
            if (getFieldCount(Field.P1) < 3 && !(currentPlayer == Player.P1 && getFieldCount(Field.MOVED) == 1)) {
                state = State.P2WIN
                return true
            }
            if (getFieldCount(Field.P2) < 3 && !(currentPlayer == Player.P2 && getFieldCount(Field.MOVED) == 1)) {
                state = State.P1WIN
                return true
            }
        }
        return if (state == State.EARLYGAME || previousState == State.EARLYGAME) {
            false
        } else when (currentPlayer) {
            Player.P1 -> {
                if (getFieldCount(Field.P1) == 3) {
                    return false
                }
                for (ring in board.indices) {
                    for (field in board[ring].indices) {
                        if (board[ring][field] == Field.P1) {
                            when (field) {
                                0 -> {
                                    if (board[ring][field + 1] == Field.EMPTY ||
                                            board[ring][7] == Field.EMPTY) {
                                        return false
                                    }
                                }

                                2, 4, 6 -> {
                                    if (board[ring][field + 1] == Field.EMPTY ||
                                            board[ring][field - 1] == Field.EMPTY) {
                                        return false
                                    }
                                }

                                1, 3, 5 -> {
                                    when (ring) {
                                        0, 2 -> {
                                            if (board[ring][field + 1] == Field.EMPTY || board[ring][field - 1] == Field.EMPTY || board[1][field] == Field.EMPTY) {
                                                return false
                                            }
                                        }

                                        1 -> {
                                            if (board[ring][field + 1] == Field.EMPTY || board[ring][field - 1] == Field.EMPTY || board[0][field] == Field.EMPTY || board[2][field] == Field.EMPTY) {
                                                return false
                                            }
                                        }
                                    }
                                }

                                7 -> {
                                    when (ring) {
                                        0, 2 -> {
                                            if (board[ring][0] == Field.EMPTY || board[ring][field - 1] == Field.EMPTY || board[1][field] == Field.EMPTY) {
                                                return false
                                            }
                                        }

                                        1 -> {
                                            if (board[ring][0] == Field.EMPTY || board[ring][field - 1] == Field.EMPTY || board[0][field] == Field.EMPTY || board[2][field] == Field.EMPTY) {
                                                return false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                state = State.P2WIN
                true
            }

            Player.P2 -> {
                if (getFieldCount(Field.P2) == 3) {
                    return false
                }
                for (ring in board.indices) {
                    for (field in board[ring].indices) {
                        if (board[ring][field] == Field.P2) {
                            when (field) {
                                0 -> {
                                    if (board[ring][field + 1] == Field.EMPTY ||
                                            board[ring][7] == Field.EMPTY) {
                                        return false
                                    }
                                }

                                2, 4, 6 -> {
                                    if (board[ring][field + 1] == Field.EMPTY ||
                                            board[ring][field - 1] == Field.EMPTY) {
                                        return false
                                    }
                                }

                                1, 3, 5 -> {
                                    when (ring) {
                                        0, 2 -> {
                                            if (board[ring][field + 1] == Field.EMPTY || board[ring][field - 1] == Field.EMPTY || board[1][field] == Field.EMPTY) {
                                                return false
                                            }
                                        }

                                        1 -> {
                                            if (board[ring][field + 1] == Field.EMPTY || board[ring][field - 1] == Field.EMPTY || board[0][field] == Field.EMPTY || board[2][field] == Field.EMPTY) {
                                                return false
                                            }
                                        }
                                    }
                                }

                                7 -> {
                                    when (ring) {
                                        0, 2 -> {
                                            if (board[ring][0] == Field.EMPTY || board[ring][field - 1] == Field.EMPTY || board[1][field] == Field.EMPTY) {
                                                return false
                                            }
                                        }

                                        1 -> {
                                            if (board[ring][0] == Field.EMPTY || board[ring][field - 1] == Field.EMPTY || board[0][field] == Field.EMPTY || board[2][field] == Field.EMPTY) {
                                                return false
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                state = State.P1WIN
                true
            }
        }
    }

    fun switchCurrentPlayer() {
        when (currentPlayer) {
            Player.P1 -> {
                currentPlayer = Player.P2
            }

            Player.P2 -> {
                currentPlayer = Player.P1
            }
        }
    }
}
