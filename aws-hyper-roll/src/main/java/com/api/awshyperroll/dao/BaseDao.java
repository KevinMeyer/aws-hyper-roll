package com.api.awshyperroll.dao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BaseDao {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    public BaseDao(NamedParameterJdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }
    
    private static final String GET_UUID = "SELECT UUID()";

    public String getUUID() throws DataAccessException {
        return jdbcTemplate.queryForObject(GET_UUID, new MapSqlParameterSource(),String.class);
    }
}
