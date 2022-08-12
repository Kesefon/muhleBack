package com.example.mühleServer

import org.springframework.hateoas.CollectionModel
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.IanaLinkRelations
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.stream.Collectors

@RestController
@CrossOrigin(origins = ["*"])
internal class GameController(private val repository: GameRepository, private val assembler: GameModelAssembler) {
    @GetMapping("/games")
    fun all(): CollectionModel<EntityModel<Game>> {
        val games = repository.findAll().stream()
            .map { game: Game? -> assembler.toModel(game) }
            .collect(Collectors.toList())
        return CollectionModel.of(
            games, WebMvcLinkBuilder.linkTo(
                WebMvcLinkBuilder.methodOn(
                    GameController::class.java
                ).all()
            ).withSelfRel()
        )
    }

    @PostMapping("/games")
    fun newGame(): ResponseEntity<*> {
        val entityModel = assembler.toModel(repository.save(Game()))
        return ResponseEntity
            .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
            .body(entityModel)
    }

    @GetMapping("/games/{id}")
    fun one(@PathVariable id: Long): EntityModel<Game> {
        val game = repository.findById(id)
            .orElseThrow { GameNotFoundException(id) }
        return assembler.toModel(game)
    }

    @PostMapping("/games/{id}/play/{ring}/{field}")
    fun play(@PathVariable id: Long, @PathVariable ring: Int, @PathVariable field: Int): ResponseEntity<*> {
        val game = repository.findById(id)
            .orElseThrow { GameNotFoundException(id) }
        game.playTurn(ring, field)
        val entityModel = assembler.toModel(repository.save(game))
        return ResponseEntity
            .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
            .body(entityModel)
    }

    @PostMapping("/games/{id}/play")
    fun play(@PathVariable id: Long): ResponseEntity<*> {
        val game = repository.findById(id)
            .orElseThrow { GameNotFoundException(id) }
        val aiPlayer = game.currentPlayer
        while (aiPlayer === game.currentPlayer && !game.checkEnd()) {
            val random = Random()
            val selectedFieldN = random.nextInt(8)
            val selectedFieldR = random.nextInt(3)
            try {
                print("[" + game.id + "]Ai try: " + game.state + " \t" + selectedFieldR + "/" + selectedFieldN)
                game.playTurn(selectedFieldR, selectedFieldN)
                print("\n")
            } catch (e: IllegalMoveException) {
                print(" [Failed]\n")
            }
        }
        val entityModel = assembler.toModel(repository.save(game))
        return ResponseEntity
            .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
            .body(entityModel)
    }

    @DeleteMapping("/games/{id}/remove")
    fun remove(@PathVariable id: Long): ResponseEntity<*> {
        repository.deleteById(id)
        return ResponseEntity.noContent().build<Any>()
    }
}
