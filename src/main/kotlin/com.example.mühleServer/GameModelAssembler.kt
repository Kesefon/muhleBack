package com.example.mühleServer

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.server.RepresentationModelAssembler
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.stereotype.Component

@Component
internal class GameModelAssembler : RepresentationModelAssembler<Game, EntityModel<Game>> {
    override fun toModel(game: Game): EntityModel<Game> {
        val gameModel = EntityModel.of(
            game,
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GameController::class.java).one(game.id)).withSelfRel(),
            WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GameController::class.java).all()).withRel("games")
        )
        if (game.state === State.EARLYGAME || game.state === State.LATEGAME || game.state === State.MOVING || game.state === State.STEALING) {
            gameModel.add(
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GameController::class.java).play(game.id))
                    .withRel("play")
            )
            gameModel.add(
                WebMvcLinkBuilder.linkTo(
                    WebMvcLinkBuilder.methodOn(GameController::class.java).remove(game.id)
                ).withRel("remove")
            )
        }
        return gameModel
    }
}
