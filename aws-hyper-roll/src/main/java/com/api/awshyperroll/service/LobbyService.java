package com.api.awshyperroll.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.api.awshyperroll.dao.Dao;
import com.api.awshyperroll.dao.LobbyDao;
import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.InitializeGameData;
import com.api.awshyperroll.model.Lobby;
import com.api.awshyperroll.model.LobbyIds;
import com.api.awshyperroll.model.Player;
import com.fasterxml.jackson.core.JsonProcessingException;


@Service
public class LobbyService {
    @Autowired
    private LobbyDao lobbyDao; 

    @Autowired 
    private GameService gameService;

    @Autowired
    private Dao dao;

    public Lobby createLobby(InitializeGameData data) throws JsonProcessingException, DataAccessException{
        //Fetch available lobby code and create host UUID
        String code =  lobbyDao.generateCode();
        String hostPlayerId = dao.getUUID();
        
        data.getPlayer().setPlayerId(hostPlayerId);
        data.getPlayer().setRole("HOST");

        // Create new game for lobby
        Game game = gameService.createGame(data);
        // Add log to game
        game.getGameLog().add("Created Lobby: " + code + ". Starting Roll:" + game.getIntialRoll());
        game.setGameStatus("INITIALIZING");
        // Create lobby for game 
        Lobby lobby =  lobbyDao.createLobby(game, code);
        game.setLobbyId(lobby.getLobbyId());
        lobby.setPlayer(data.getPlayer());
        // Create host player/update game in db
        lobbyDao.createPlayer(data.getPlayer(), lobby.getLobbyId());
        gameService.updateGame(game);

        return lobby;
    }

    public LobbyIds joinLobby (String code, Player player ) throws JsonProcessingException, DataAccessException {
        // Create new player
        String playerId = dao.getUUID();
        player.setPlayerId(playerId);
        player.setRole("PLAYER");

        // Update game with new player and log 
        Lobby lobby = lobbyDao.getLobby(code);
        Game game = gameService.getGame(lobby.getGameId());
        game.getGameLog().add("Player " + player.getName() + " has joined the game");
        game.getPlayers().add(player);
        
        // Create player and update game in db
        lobbyDao.createPlayer(player, lobby.getLobbyId());
        gameService.updateGame(game);

        LobbyIds lobbyIds = new LobbyIds();
        lobbyIds.setGameId(lobby.getGameId());
        lobbyIds.setPlayerId(playerId);;
        return lobbyIds;

    }

    public boolean pollPlayerRefresh(String playerId) {
        return lobbyDao.pollPlayerRefresh(playerId);
    }

    public void setLatestGameFlag(String playerId, boolean flag){
        lobbyDao.setLatestGameFlag(playerId, flag);
    }
}
