package com.api.awshyperroll.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.awshyperroll.Constants.GenericConstants;
import com.api.awshyperroll.dao.BaseDao;
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
    private BaseDao dao;

    public LobbyIds createLobby(InitializeGameData data) throws JsonProcessingException {
        //Fetch available lobby code and create host UUID
        String code =  lobbyDao.generateCode();
        String hostPlayerId = dao.getUUID();
        String lobbyId = dao.getUUID();

        
        Player initPlayer = data.getPlayers().get(0);

        initPlayer.setPlayerId(hostPlayerId);
        initPlayer.setRole(GenericConstants.HOST);

        // Create new game for lobby
        String initMessage = "Created Lobby: " + code + ". Starting Roll:" + data.getInitRoll();
        Game game = gameService.createGame(data, lobbyId, initMessage );
        // Add log to game
        // Create lobby for game 
        Lobby lobby =  lobbyDao.createLobby(game, lobbyId, code);
        lobby.setPlayer(initPlayer);
        // Create host player/update game in db
        lobbyDao.createPlayer(initPlayer,lobbyId);
        gameService.updateGame(game);

        LobbyIds ids = new LobbyIds();
        ids.setGameId(lobby.getGameId());
        ids.setPlayerId(lobby.getPlayer().getPlayerId());
        ids.setLobbyId(lobby.getLobbyId());

        return ids;
    }

    public LobbyIds joinLobby (String code, Player player ) throws JsonProcessingException {
        // Create new player
        String playerId = dao.getUUID();
        player.setPlayerId(playerId);
        player.setRole(GenericConstants.PLAYER);

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
        lobbyIds.setLobbyId(lobby.getLobbyId());
        return lobbyIds;

    }

    public boolean pollPlayerRefresh(String playerId) {
        return lobbyDao.pollPlayerRefresh(playerId);
    }

    public void setLatestGameFlag(String playerId, boolean flag) {
        lobbyDao.setLatestGameFlag(playerId, flag);
    }

    public void changeLobbyActvFlag(String gameId, boolean flag) {
        lobbyDao.changeLobbyActvFlag(gameId, flag);
    }

    public void updateLobbyGame(String lobbyId, String gameId) {
        lobbyDao.updateLobbyGame(lobbyId, gameId);
    }
}
