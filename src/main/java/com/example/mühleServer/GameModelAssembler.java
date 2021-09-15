package com.example.mühleServer;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
class GameModelAssembler implements RepresentationModelAssembler<Game, EntityModel<Game>> {

    @Override
    public EntityModel<Game> toModel(Game game) {
        EntityModel<Game> gameModel = EntityModel.of(game,
                linkTo(methodOn(GameController.class).one(game.getId())).withSelfRel(),
                linkTo(methodOn(GameController.class).all()).withRel("games"));

        if (game.getState() == State.EARLYGAME || game.getState() == State.LATEGAME || game.getState() == State.MOVING || game.getState() == State.STEALING) {
            gameModel.add(linkTo(methodOn(GameController.class).play(game.getId())).withRel("play"));
            gameModel.add(linkTo(methodOn(GameController.class).remove(game.getId())).withRel("remove"));
        }
        return gameModel;
    }
}
