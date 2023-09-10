package com.api.awshyperroll.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.Roll;
import com.api.awshyperroll.service.GameService;
import com.fasterxml.jackson.core.JsonProcessingException;


@RestController
public class GameController { 

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameService gameService; 

    @PostMapping("/game")
    public Game createNewGame(@RequestBody InitializeGameData gameData) {
        try {
            LOGGER.info("Begin creating Game...");
            Game game =  gameService.createGame(gameData);
            game.getGameLog().add("Press Start Game to roll!");
            game.setGameStatus("PLAYING");
            return game;
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Game";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occured while creating new Game";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }

    @PostMapping("/game/roll")
    public Game roll(@RequestBody Game game){
        try {
            LOGGER.info("Begin rolling...");
            Game serverGame = gameService.getGame(game.getGameId()); 
            Roll roll = serverGame.roll();
            gameService.insertRoll(roll);
            gameService.updateGame(serverGame);
            return serverGame;
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Game";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occured while rolling";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    } 

    @GetMapping("/game/{gameId}")
    public Game getGame(@PathVariable String gameId) {
        try {
            LOGGER.info("Begin getting Game...");
            return gameService.getGame(gameId);
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Game";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occured while creating new Game";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }

    @GetMapping("/game/poll/{gameId}")
    public Game pollGameState(@PathVariable String gameId){
        try {
            LOGGER.info("Begin polling GameState for " + gameId + "..." );
            // Fetch the intial game from the db and set initial player/rolls counts
            Game game =  gameService.getGame(gameId);
            int players = game.getPlayers().size();
            int rolls = game.getRolls().size(); 

            for (int i = 0; i < 40; i++){
                LOGGER.info("Tick " + i);
                game = gameService.getGame(gameId);
                if ("INITIALIZING".equals(game.getGameStatus())) {
                    game.setGameStatus("PLAYING");
                    gameService.updateGame(game);
                    return game;
                }
                if( players != game.getPlayers().size() || rolls !=  game.getRolls().size() ){
                    return game;
                }
                Thread.sleep(250L);
            }
            return game;
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Game";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occured while creating new Game";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        } catch (InterruptedException ie) {
            String message = "Error occured while polling for new game state";
            LOGGER.error(message, ie);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }

}
