package com.api.awshyperroll.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Repository
public class AccountDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDao.class);
   
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();

    public AccountDao(NamedParameterJdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    private static final String INSERT_EMAIL_AUTH = "INSERT INTO email_auth (email, code, create_ts) VALUES (:email, :code, CURRENT_TIMESTAMP()) "
                                                  + "ON DUPLICATE KEY UPDATE code = :code, create_ts =  CURRENT_TIMESTAMP();";

    @Autowired
    private Dao dao;

    public void createAuthRecord(String email, String code) {
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.EMAIL,email )
            .addValue(DaoConstants.CODE, code);
        
        jdbcTemplate.update(INSERT_EMAIL_AUTH, source);

    }
}
