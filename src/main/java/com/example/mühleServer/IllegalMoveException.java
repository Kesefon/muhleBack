package com.example.mühleServer;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

class IllegalMoveException  extends RuntimeException {

    IllegalMoveException(State state, int ring, int field) {
        super("Illegal Move: State: " + state + " Move: " + ring + "/" + field);
    }
}

@ControllerAdvice
class IllegalMoveAdvice {

    @ResponseBody
    @ExceptionHandler(IllegalMoveException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    String illegalMoveHandler(IllegalMoveException ex) {
        return ex.getMessage();
    }
}