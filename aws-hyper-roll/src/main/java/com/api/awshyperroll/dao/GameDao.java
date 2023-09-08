package com.api.awshyperroll.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.api.awshyperroll.model.Game;
import com.api.awshyperroll.model.Roll;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class GameDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();
    public GameDao(NamedParameterJdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    private static final String GET_UUID = "SELECT UUID()";
    private static final String INSERT_ROLL = "INSERT INTO hyperrolldb.roll_hist (roll, player_nm) VALUES (:roll, :player_nm)";
    private static final String INSERT_GAME = "INSERT INTO hyperrolldb.game (id, json) VALUES (:id, :json)";
    private static final String GET_GAME = "SELECT json FROM hyperrolldb.game g WHERE g.id = :id ";
    private static final String UPDATE_GAME = "UPDATE hyperrolldb.game SET json = :json WHERE id = :id";

    public void createGame(Game game) throws JsonProcessingException, DataAccessException {
        String id = getUUID();
        game.setId(id);
        String gameJSON = mapper.writeValueAsString(game);
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue("id", id)
            .addValue("json", gameJSON);
        jdbcTemplate.update(INSERT_GAME, source);
    }

    public void updateGame(Game game) throws JsonProcessingException, DataAccessException {
        String gameJSON = mapper.writeValueAsString(game);
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue("id", game.getId())
            .addValue("json", gameJSON);
        jdbcTemplate.update(UPDATE_GAME, source);

    }
    public Game getGame(String id) throws JsonProcessingException, DataAccessException {
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue("id", id); 
        String gameJSON = jdbcTemplate.queryForObject(GET_GAME, source, String.class);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(gameJSON, Game.class);
    }

    public String getUUID() throws DataAccessException {
        return jdbcTemplate.queryForObject(GET_UUID, new MapSqlParameterSource(),String.class);
    }
    public void insertRoll (Roll roll) throws DataAccessException{
        SqlParameterSource parameterSource = new MapSqlParameterSource()
            .addValue("roll", roll.getRoll())
            .addValue("player_nm", roll.getPlayer());

            jdbcTemplate.update(INSERT_ROLL, parameterSource);
    }

}
