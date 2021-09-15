package com.example.mühleServer;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;

import java.util.Objects;
import java.util.Arrays;

@Entity
class Game {

    private @Id @GeneratedValue Long id;
    private Player currentPlayer;
    private State state;
    private State previousState;
    private int movedFieldR;
    private int movedFieldN;
    private int turn;
    private @Lob Field[][] board;

    Game() {
        this.currentPlayer = Player.P1;
        this.board = new Field[3][8];
        for (int i = 0; i < this.board.length; i++) {
            Arrays.fill(this.board[i], Field.EMPTY);
        }
        this.state = State.EARLYGAME;
        this.previousState = this.state;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Player getCurrentPlayer() { return this.currentPlayer; }

    public void setCurrentPlayer(Player player) { this.currentPlayer = currentPlayer; }

    public Field[][] getBoard() { return this.board; }

    public void setBoard(Field[][] board) { this.board = board; }

    public State getState() { return this.state; }

    public void setState(State state) { this.state = state; }

    private State getPreviousState() { return this.previousState; }

    private void setPreviousState(State state) { this.previousState = state; }

    public int getTurn() { return this.turn; }

    public void setTurn(int turn) { this.turn = turn; }

    private int getFieldCount(Field field) {
        int result = 0;
        for (int i=0; i < this.board.length; i++) {
            for (int j=0; j < this.board[i].length; j++) {
                if (this.board[i][j] == field) {
                    result++;
                }
            }
        }
        return result;
    }

    public boolean checkMühle(int ring, int field) {
        if (this.board[ring][field] == Field.EMPTY) { return false; }
        switch (field) {
            case 0 -> {
                if ((this.board[ring][field] == this.board[ring][field + 1] &&
                        this.board[ring][field] == this.board[ring][field + 2]) ||
                        (this.board[ring][0] == this.board[ring][7] &&
                                this.board[ring][0]== this.board[ring][6])) {
                    return true;
                }
            }
            case 2,4 -> {
                if ((this.board[ring][field] == this.board[ring][field + 1] &&
                        this.board[ring][field]== this.board[ring][field + 2]) ||
                        (this.board[ring][field] == this.board[ring][field - 1] &&
                                this.board[ring][field] == this.board[ring][field - 2])) {
                    return true;
                }
            }
            case 6 -> {
                if ((this.board[ring][field] == this.board[ring][field - 1] &&
                        this.board[ring][field] == this.board[ring][field - 2]) ||
                        (this.board[ring][0] == this.board[ring][7] &&
                                this.board[ring][0]== this.board[ring][6])) {
                    return true;
                }
            }
            case 1,3,5 -> {
                if ((this.board[0][field] == this.board[1][field] &&
                        this.board[0][field] == this.board[2][field]) ||
                        (this.board[ring][field] == this.board[ring][field + 1] &&
                                this.board[ring][field] == this.board[ring][field - 1])){
                    return true;
                }
            }
            case 7 -> {
                if ((this.board[0][field] == this.board[1][field] &&
                        this.board[0][field] == this.board[2][field]) ||
                        (this.board[ring][field] == this.board[ring][0] &&
                                this.board[ring][field] == this.board[ring][field - 1])) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkOnlyMühle(Player player) {
        for (int i=0; i < this.board.length; i++) {
            for (int j = 0; j < this.board[i].length; j++) {
                switch (player) {
                    case P1 -> { if (this.board[i][j] == Field.P1) {
                        if (!checkMühle(i, j)) { return false; }
                    }}
                    case P2 -> { if (this.board[i][j] == Field.P2) {
                        if (!checkMühle(i, j)) { return false;}
                    }}
                }
            }
        }
        return true;
    }

    public void playTurn(int selectedFieldR, int selectedFieldN) {
        if ((0 > selectedFieldN || selectedFieldN > 7) && (0 > selectedFieldR || selectedFieldR > 2)) {
            throw new IllegalMoveException(this.state, selectedFieldR, selectedFieldN);
        }
        switch (this.state) {
            case EARLYGAME -> {
                if(this.board[selectedFieldR][selectedFieldN] == Field.EMPTY) {
                    switch (this.currentPlayer) {
                        case P1 -> { this.board[selectedFieldR][selectedFieldN] = Field.P1; }
                        case P2 -> { this.board[selectedFieldR][selectedFieldN] = Field.P2; }
                    }
                    this.turn++;
                    if (this.turn == 18) {
                        this.state = State.LATEGAME;
                    }
                    this.checkEnd();
                    if (this.checkMühle(selectedFieldR, selectedFieldN)) {
                        this.previousState = this.state;
                        this.state = State.STEALING;
                        return;
                    }
                    this.switchCurrentPlayer();
                    this.checkEnd();
                }
                else { throw new IllegalMoveException(this.state, selectedFieldR, selectedFieldN); }
                return;
            }
            case LATEGAME -> {
                this.previousState = State.LATEGAME;
                switch (this.currentPlayer) {
                    case P1 -> { if(this.board[selectedFieldR][selectedFieldN] != Field.P1){
                        throw new IllegalMoveException(this.state, selectedFieldR, selectedFieldN);
                    }}
                    case P2 -> { if(this.board[selectedFieldR][selectedFieldN] != Field.P2){
                        throw new IllegalMoveException(this.state, selectedFieldR, selectedFieldN);
                    }}
                }
                this.board[selectedFieldR][selectedFieldN] = Field.MOVED;
                movedFieldR = selectedFieldR;
                movedFieldN = selectedFieldN;
                this.state = State.MOVING;
                return;
            }
            case STEALING -> {
                switch (this.currentPlayer) {
                    case P1 -> { if((this.board[selectedFieldR][selectedFieldN] != Field.P2) ||
                            (this.checkMühle(selectedFieldR,selectedFieldN) && !checkOnlyMühle(Player.P2))){
                        throw new IllegalMoveException(this.state, selectedFieldR, selectedFieldN);
                    }}
                    case P2 -> { if((this.board[selectedFieldR][selectedFieldN] != Field.P1) ||
                            (this.checkMühle(selectedFieldR,selectedFieldN) && !checkOnlyMühle(Player.P1))){
                        throw new IllegalMoveException(this.state, selectedFieldR, selectedFieldN);
                    }}
                }
                this.board[selectedFieldR][selectedFieldN] = Field.EMPTY;
                this.state = this.previousState;
                this.switchCurrentPlayer();
                this.checkEnd();
                return;
            }
            case MOVING -> {
                if (this.board[selectedFieldR][selectedFieldN] == Field.MOVED) {
                    switch (this.currentPlayer) {
                        case P1 -> { this.board[selectedFieldR][selectedFieldN] = Field.P1; }
                        case P2 -> { this.board[selectedFieldR][selectedFieldN] = Field.P2; }
                    }
                    movedFieldN = -5;
                    movedFieldR = -5;
                    this.state = State.LATEGAME;
                    return;
                }
                if (this.board[selectedFieldR][selectedFieldN] != Field.EMPTY) {
                    throw new IllegalMoveException(this.state, selectedFieldR, selectedFieldN);
                }
                boolean jump = false;
                switch (this.currentPlayer) {
                    case P1 -> { if (getFieldCount(Field.P1) < 3) { jump = true; } }
                    case P2 -> { if (getFieldCount(Field.P2) < 3) { jump = true; } }
                }
                if (((Math.abs(selectedFieldR - movedFieldR) == 1 && selectedFieldN == movedFieldN &&
                        selectedFieldN%2 == 1) ||
                        (Math.abs(selectedFieldN - movedFieldN) == 1 && selectedFieldR == movedFieldR) ||
                        (selectedFieldN == 0 && movedFieldN == 7 && selectedFieldR == movedFieldR) ||
                        (selectedFieldN == 7 && movedFieldN == 0 && selectedFieldR == movedFieldR)) ||
                        jump
                ) {
                    switch (this.currentPlayer) {
                        case P1 -> { this.board[selectedFieldR][selectedFieldN] = Field.P1; }
                        case P2 -> { this.board[selectedFieldR][selectedFieldN] = Field.P2; }
                    }
                    this.board[movedFieldR][movedFieldN] = Field.EMPTY;
                    movedFieldN = -5;
                    movedFieldR = -5;
                    this.state = State.LATEGAME;
                    this.turn++;
                    if(checkMühle(selectedFieldR, selectedFieldN)) {
                        this.state = State.STEALING;
                    } else {
                        this.switchCurrentPlayer();
                        this.checkEnd();
                    }
                }
            }
        }
    }

    public boolean checkEnd() {
        if (this.turn <= 18) {
            if ((this.getFieldCount(Field.P1) + ((17 - this.turn) / 2)) < 3) { this.state = State.P2WIN; return true; }
            if ((this.getFieldCount(Field.P2) + ((18 - this.turn) / 2)) < 3) { this.state = State.P1WIN; return true; }
        }
        else {
            if (this.getFieldCount(Field.P1) < 3 && !(this.currentPlayer == Player.P1 && this.getFieldCount(Field.MOVED) == 1)) {
                this.state = State.P2WIN; return true; }
            if (this.getFieldCount(Field.P2) < 3 && !(this.currentPlayer == Player.P2 && this.getFieldCount(Field.MOVED) == 1)) {
                this.state = State.P1WIN; return true; }
        }

        if (this.state == State.EARLYGAME || this.previousState == State.EARLYGAME) { return false; }

        switch (this.currentPlayer) {
            case P1 -> {
                if (getFieldCount(Field.P1) == 3) { return false; }
                for (int ring=0; ring < this.board.length; ring++) {
                    for (int field=0; field < this.board[ring].length; field++) {
                        if (this.board[ring][field] == Field.P1) {
                            switch (field) {
                                case 0 -> {
                                    if (this.board[ring][field + 1] == Field.EMPTY ||
                                            this.board[ring][7] == Field.EMPTY) {
                                        return false;
                                    }
                                }
                                case 2,4,6 -> {
                                    if (this.board[ring][field + 1] == Field.EMPTY ||
                                            this.board[ring][field - 1] == Field.EMPTY) {
                                        return false;
                                    }
                                }
                                case 1,3,5 -> {
                                    switch (ring) {
                                    case 0,2 -> {
                                        if (this.board[ring][field + 1] == Field.EMPTY ||
                                            this.board[ring][field - 1] == Field.EMPTY ||
                                            this.board[1][field] == Field.EMPTY) {
                                            return false;
                                        }
                                    }
                                    case 1 -> {
                                        if (this.board[ring][field + 1] == Field.EMPTY ||
                                                this.board[ring][field - 1] == Field.EMPTY ||
                                                this.board[0][field] == Field.EMPTY ||
                                                this.board[2][field] == Field.EMPTY) {
                                            return false;
                                        }
                                    }
                                    }
                                }
                                case 7 -> {
                                    switch (ring) {
                                        case 0,2 -> {
                                            if (this.board[ring][0] == Field.EMPTY ||
                                                    this.board[ring][field - 1] == Field.EMPTY ||
                                                    this.board[1][field] == Field.EMPTY) {
                                                return false;
                                            }
                                        }
                                        case 1 -> {
                                            if (this.board[ring][0] == Field.EMPTY ||
                                                    this.board[ring][field - 1] == Field.EMPTY ||
                                                    this.board[0][field] == Field.EMPTY ||
                                                    this.board[2][field] == Field.EMPTY) {
                                                return false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                this.state = State.P2WIN;
                return true;
            }
            case P2 -> {
                if (getFieldCount(Field.P2) == 3) { return false; }
                for (int ring=0; ring < this.board.length; ring++) {
                    for (int field=0; field < this.board[ring].length; field++) {
                        if (this.board[ring][field] == Field.P2) {
                            switch (field) {
                                case 0 -> {
                                    if (this.board[ring][field + 1] == Field.EMPTY ||
                                            this.board[ring][7] == Field.EMPTY) {
                                        return false;
                                    }
                                }
                                case 2,4,6 -> {
                                    if (this.board[ring][field + 1] == Field.EMPTY ||
                                            this.board[ring][field - 1] == Field.EMPTY) {
                                        return false;
                                    }
                                }
                                case 1,3,5 -> {
                                    switch (ring) {
                                        case 0,2 -> {
                                            if (this.board[ring][field + 1] == Field.EMPTY ||
                                                    this.board[ring][field - 1] == Field.EMPTY ||
                                                    this.board[1][field] == Field.EMPTY) {
                                                return false;
                                            }
                                        }
                                        case 1 -> {
                                            if (this.board[ring][field + 1] == Field.EMPTY ||
                                                    this.board[ring][field - 1] == Field.EMPTY ||
                                                    this.board[0][field] == Field.EMPTY ||
                                                    this.board[2][field] == Field.EMPTY) {
                                                return false;
                                            }
                                        }
                                    }
                                }
                                case 7 -> {
                                    switch (ring) {
                                        case 0,2 -> {
                                            if (this.board[ring][0] == Field.EMPTY ||
                                                    this.board[ring][field - 1] == Field.EMPTY ||
                                                    this.board[1][field] == Field.EMPTY) {
                                                return false;
                                            }
                                        }
                                        case 1 -> {
                                            if (this.board[ring][0] == Field.EMPTY ||
                                                    this.board[ring][field - 1] == Field.EMPTY ||
                                                    this.board[0][field] == Field.EMPTY ||
                                                    this.board[2][field] == Field.EMPTY) {
                                                return false;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                this.state = State.P1WIN;
                return true;
            }
        }
        return false;
    }

    public void switchCurrentPlayer() {
        switch (this.currentPlayer) {
            case P1 -> { this.currentPlayer = Player.P2; }
            case P2 -> { this.currentPlayer = Player.P1; }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Game))
            return false;
        Game game = (Game) o;
        return Objects.equals(this.id, game.id) && Arrays.deepEquals(this.board, game.board) && Objects.equals(this.currentPlayer, game.currentPlayer) && Objects.equals(this.state, game.state);
    }

    @Override
    public int hashCode() { return Objects.hash(this.id, Arrays.hashCode(this.board), this.currentPlayer, this.state); }

    @Override
    public String toString() {
        return "Game{" + "id=" + this.id + ", board=" + Arrays.deepToString(this.board) + ", currentPlayer=" + this.currentPlayer + ", state=" + this.state + "}";
    }
}
