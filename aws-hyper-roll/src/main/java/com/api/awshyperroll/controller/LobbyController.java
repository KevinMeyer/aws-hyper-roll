package com.api.awshyperroll.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.LobbyIds;
import com.api.awshyperroll.model.Player;
import com.api.awshyperroll.model.PollingResponse;
import com.api.awshyperroll.service.GameService;
import com.api.awshyperroll.service.LobbyService;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class LobbyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LobbyController.class);

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private GameService gameService;
    
    @PostMapping("/lobby")
    public LobbyIds createLobby (@RequestBody InitializeGameData gameData) {
        LOGGER.info("Begin createLobby...");
        try {
            // Create the new lobby and store in database            
            // Returns required ids after creating lobby for game to function
            return lobbyService.createLobby(gameData);
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Lobby";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occurred while creating new Lobby";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }
   
    @PatchMapping("/lobby/join/{lobbyCode}")
    public LobbyIds joinLobby ( @PathVariable String lobbyCode,
                                @RequestBody Player player ) {

        LOGGER.info("Begin joining Lobby...");
        try {
            // Returns required Ids after joining lobby for game to function
           return  lobbyService.joinLobby(lobbyCode, player);
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when joining new Lobby";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occurred while creating new Lobby";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }        
    }
    

    @GetMapping("/lobby/{lobbyId}/player/{playerId}")
    public PollingResponse pollLobbyState(@PathVariable String lobbyId, @PathVariable String playerId){
        try {
            LOGGER.info("Begin polling GameState for player " + playerId );
            LobbyIds lobbyIds = new LobbyIds();
            PollingResponse response = new PollingResponse();

            lobbyIds.setLobbyId(lobbyId);
            lobbyIds.setPlayerId(playerId);

            for (int i = 0; i < 40; i++){
                boolean foo = lobbyService.pollPlayerRefresh(playerId); 
                if(!foo){
                    LOGGER.info("Game update found!");
                    lobbyService.setLatestGameFlag(playerId, true);
                    Game polledGame = gameService.getGameByLobbyId(lobbyId);
                    response = new PollingResponse();
                    response.setGame(polledGame);
                    lobbyIds.setGameId(polledGame.getGameId());
                    response.setLobbyIds(lobbyIds);
                    return response;
                }
                Thread.sleep(250L);
            }
            LOGGER.info("No game update found, return latest version of game.");
            Game serverGame = gameService.getGameByLobbyId(lobbyId);
            response.setGame(serverGame);
            lobbyIds.setGameId(serverGame.getGameId());
            response.setLobbyIds(lobbyIds);
            return response;
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Game";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occurred while creating new Game";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        } catch (InterruptedException ie) {
            String message = "Error occurred while polling for new game state";
            LOGGER.error(message, ie);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }

    @PatchMapping("/lobby/reset")
    public void resetLobby(@RequestBody LobbyIds lobbyIds){
        LOGGER.info("Begin resetting Lobby... lobby_id: " + lobbyIds.getLobbyId() );
        try {
            //Get the last game played in this lobby
            Game currentGame = gameService.getGame(lobbyIds.getGameId());
           
            // Set data to create new game
            InitializeGameData gameData = new InitializeGameData();
            gameData.setBotGame(false);
            gameData.setInitRoll(currentGame.getInitialRoll());
            gameData.setPlayers(new ArrayList<>(currentGame.getPlayers()));
            
            //Create the new game
            String initMessage = "Game Reset! Starting Roll:" + currentGame.getInitialRoll();
            Game newGame = gameService.createGame(gameData, lobbyIds.getLobbyId(), initMessage);

            // Update lobby to point at new game
            lobbyService.updateLobbyGame(lobbyIds.getLobbyId(), newGame.getGameId());
            LOGGER.info("Set lobby to game_id: " + newGame.getGameId());

            //Push updates to players
            gameService.pushUpdateToPlayer(lobbyIds.getLobbyId());
            lobbyService.changeLobbyActvFlag(lobbyIds.getLobbyId(), true);

        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when joining new Lobby";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occurred while creating new Lobby";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }        
    }

    @PostMapping("lobby/{lobbyId}/player/{playerId}/leave")
    public void leaveLobby(@PathVariable String lobbyId, @PathVariable String playerId){
        LOGGER.info("Begin leaveLobby for lobby_id: " + lobbyId + " player_id: " + playerId);
        // Flag update to player to exit polling loop once player leaves lobby
        gameService.pushUpdateToPlayer(lobbyId);
    }
}
