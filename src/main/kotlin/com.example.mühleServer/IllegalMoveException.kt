package com.example.mühleServer

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

internal class IllegalMoveException(state: State, ring: Int, field: Int) :
    RuntimeException("Illegal Move: State: $state Move: $ring/$field")

@ControllerAdvice
internal class IllegalMoveAdvice {
    @ResponseBody
    @ExceptionHandler(IllegalMoveException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun illegalMoveHandler(ex: IllegalMoveException): String? {
        return ex.message
    }
}
