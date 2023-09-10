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

import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.Lobby;
import com.api.awshyperroll.model.LobbyIds;
import com.api.awshyperroll.model.Player;
import com.api.awshyperroll.service.LobbyService;
import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class LobbyController {
    private static final Logger LOGGER = LoggerFactory.getLogger(LobbyController.class);

    @Autowired
    private LobbyService lobbyService;
    
    @PostMapping("/lobby")
    public LobbyIds createLobby (@RequestBody InitializeGameData gameData) {
        LOGGER.info("Begin createLobby...");
        try {
            Lobby lobby = lobbyService.createLobby(gameData);
            LobbyIds ids = new LobbyIds();
            ids.setGameId(lobby.getGameId());
            ids.setPlayerId(lobby.getPlayer().getPlayerId());
            return ids;
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when creating new Lobby";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occured while creating new Lobby";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }
    }
    @GetMapping("/lobby/{lobbyCode}")
    public Lobby getLobby(@PathVariable String lobbyCode){
        LOGGER.info("Begin getLobby...");
        return null;
    }
    
   
    @PatchMapping("/lobby/join/{lobbyCode}")
    public LobbyIds joinLobby (@PathVariable String lobbyCode,
                            @RequestBody Player player){

        LOGGER.info("Begin joining Lobby...");
        try {
           LobbyIds ids = lobbyService.joinLobby(lobbyCode, player);;
           return ids;
        } catch (JsonProcessingException jpe) {
            String message = "JSON Parse failed when joining new Lobby";
            LOGGER.error( message, jpe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
        } catch (DataAccessException dae) {
            String message = "Database error occured while creating new Lobby";
            LOGGER.error(message, dae);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message );
        }        
    }
}
