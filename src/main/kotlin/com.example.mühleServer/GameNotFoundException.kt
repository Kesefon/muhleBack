package com.example.mühleServer

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

internal class GameNotFoundException(id: Long) : RuntimeException("Could not find game $id")

@ControllerAdvice
internal class GameNotFoundAdvice {
    @ResponseBody
    @ExceptionHandler(GameNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun gameNotFoundHandler(ex: GameNotFoundException): String? {
        return ex.message
    }
}
