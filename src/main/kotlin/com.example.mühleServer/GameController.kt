package com.example.mühleServer;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
class GameController {

    private final GameRepository repository;

    private final GameModelAssembler assembler;

    GameController(GameRepository repository, GameModelAssembler assembler) {
        this.repository = repository;
        this.assembler = assembler;
    }

    @GetMapping("/games")
    CollectionModel<EntityModel<Game>> all() {
        List<EntityModel<Game>> games = repository.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        return CollectionModel.of(games, linkTo(methodOn(GameController.class).all()).withSelfRel());
    }

    @PostMapping("/games")
    ResponseEntity<?> newGame() {

        EntityModel<Game> entityModel = assembler.toModel(repository.save(new Game()));
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/games/{id}")
    EntityModel<Game> one(@PathVariable Long id) {
        Game game = repository.findById(id)
                .orElseThrow( () -> new GameNotFoundException(id));
        return assembler.toModel(game);
    }

    @PostMapping("/games/{id}/play/{ring}/{field}")
    ResponseEntity<?> play(@PathVariable Long id,@PathVariable int ring, @PathVariable int field) {
        Game game = repository.findById(id)
                .orElseThrow( () -> new GameNotFoundException(id));
        game.playTurn(ring, field);
        EntityModel<Game> entityModel = assembler.toModel(repository.save(game));
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @PostMapping("/games/{id}/play")
    ResponseEntity<?> play(@PathVariable Long id) {
        Game game = repository.findById(id)
                .orElseThrow( () -> new GameNotFoundException(id));
        Player aiPlayer = game.getCurrentPlayer();
        while (aiPlayer == game.getCurrentPlayer() && !game.checkEnd()) {
            Random random = new Random();
            int selectedFieldN = random.nextInt(8);
            int selectedFieldR = random.nextInt(3);
            try {
                System.out.print("[" + game.getId() + "]Ai try: " + game.getState() + " \t" + selectedFieldR + "/" + selectedFieldN);
                game.playTurn(selectedFieldR, selectedFieldN);
                System.out.print("\n");
            } catch (IllegalMoveException e) {
                System.out.print(" [Failed]\n");
            }
        }
        EntityModel<Game> entityModel = assembler.toModel(repository.save(game));
        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @DeleteMapping("/games/{id}/remove")
    ResponseEntity<?> remove(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
