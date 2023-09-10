package com.api.awshyperroll.dao;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.Lobby;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class LobbyDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private Dao dao;
    public LobbyDao(NamedParameterJdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    
    private static final String CREATE_LOBBY = "INSERT INTO hyperrolldb.lobby (lobby_id,code,lobby_json,game_id,upd_ts,actv_flag) " + 
                                                "VALUES (:lobby_id,:code,:lobby_json,:game_id,CURRENT_TIMESTAMP(),1);";
    private static final String GET_LOBBY = "SELECT game_id FROM hyperrolldb.lobby WHERE code = :code AND actv_flag = 1; ";
    // private static final String UPDATE_LOBBY  = "";
    private static final String CODE_COUNT = "SELECT COUNT(code) FROM hyperrolldb.lobby WHERE code = :code AND actv_flag != 0;";

   

    public Lobby createLobby(Game game, String code) throws JsonProcessingException, DataAccessException {
        String lobbyId = dao.getUUID();
        Lobby lobby = new Lobby();
        lobby.setLobbyId(lobbyId);
        lobby.setCode(code);
        lobby.setGameId(game.getGameId());
        String lobbyJSON = mapper.writeValueAsString(lobby);

        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.LOBBY_ID, lobbyId)
            .addValue(DaoConstants.CODE, code)
            .addValue(DaoConstants.LOBBY_JSON, lobbyJSON)
            .addValue(DaoConstants.GAME_ID, lobby.getGameId());
        
        jdbcTemplate.update(CREATE_LOBBY, source);
        return lobby;



    }

    public String getLobbyGame(String code) throws JsonProcessingException, DataAccessException {
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.CODE, code);
        return jdbcTemplate.queryForObject(GET_LOBBY, source, String.class);
    }

    public String generateCode() {
        String code = createRandomCode();
        boolean validCode = false;
        SqlParameterSource source;
        int count;
        while (!validCode) {
            source = new MapSqlParameterSource()
                .addValue(DaoConstants.CODE, code);
           count = jdbcTemplate.queryForObject(CODE_COUNT, source, int.class);
           if (count == 0){
                validCode = true;
           } else {
                code = createRandomCode();
           }
        }
        return code.toUpperCase();
    }

    private String createRandomCode() {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 4;
        Random random = new Random();
    
        String code = random.ints(leftLimit, rightLimit + 1)
          .limit(targetStringLength)
          .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
          .toString();

        return code;
    }
}
