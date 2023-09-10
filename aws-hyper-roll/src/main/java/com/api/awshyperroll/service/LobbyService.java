package com.api.awshyperroll.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.api.awshyperroll.dao.LobbyDao;
import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.Lobby;
import com.api.awshyperroll.model.Player;
import com.fasterxml.jackson.core.JsonProcessingException;


@Service
public class LobbyService {
    @Autowired
    private LobbyDao lobbyDao; 

    @Autowired 
    private GameService gameService;

    public Lobby createLobby(InitializeGameData data) throws JsonProcessingException, DataAccessException{
        //Fetch available lobby code
        String code =  lobbyDao.generateCode();
        // Create new game for lobby
        Game game = gameService.createGame(data);
        // Add log to game
        game.getGameLog().add("Lobby " + code + " created. Press Start Game to begin!");
        game.setGameStatus("INITIALIZING");
        gameService.updateGame(game);
        // Create lobby for game 
        Lobby lobby =  lobbyDao.createLobby(game, code);
        lobby.setInitGame(game);
        return lobby;
    }

    public void joinLobby (String code, Player player ) throws JsonProcessingException, DataAccessException {
        String gameId = lobbyDao.getLobbyGame(code);
        Game game = gameService.getGame(gameId);
        game.getGameLog().add("Player " + player.getName() + " has joined the game");
        game.getPlayers().add(player);
        gameService.updateGame(game);

    }

}
