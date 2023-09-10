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
import com.api.awshyperroll.service.LobbyService;
import com.fasterxml.jackson.core.JsonProcessingException;


@RestController
public class GameController { 

    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    @Autowired
    private GameService gameService; 

    @Autowired 
    private LobbyService lobbyService;

    @PostMapping("/game")
    public Game createNewGame(@RequestBody InitializeGameData gameData) {
        try {
            LOGGER.info("Begin creating Game...");
            Game game =  gameService.createGame(gameData);
            game.getGameLog().add("Press Start Game to roll!");
            game.setGameStatus("PLAYING");
            LOGGER.info("Finished creating Game...");
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
            LOGGER.info("Finished rolling...");
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

    @GetMapping("/game/{gameId}/player/{playerId}")
    public Game pollGameState(@PathVariable String gameId, @PathVariable String playerId){
        try {
            LOGGER.info("Begin polling GameState for player " + playerId );
            // Fetch the intial game from the db and set initial player/rolls counts
            for (int i = 0; i < 40; i++){
                if(!lobbyService.pollPlayerRefresh(playerId)){
                    LOGGER.info("Game update found!");
                    lobbyService.setLatestGameFlag(playerId, true);
                    return gameService.getGame(gameId);
                }
                Thread.sleep(250L);
            }
            LOGGER.info("No game update found, return latest version of game.");
            return gameService.getGame(gameId);
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
