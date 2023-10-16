package com.api.awshyperroll.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Repository;

import com.api.awshyperroll.model.Account;
import com.api.awshyperroll.model.LoginRequest;
import com.api.awshyperroll.model.LoginResponse;
import com.api.awshyperroll.model.RegisterAccountInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.micrometer.common.util.StringUtils;


@Repository
public class AccountDao {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountDao.class);

   
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ObjectMapper mapper = new ObjectMapper();

    public AccountDao(NamedParameterJdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    private static final String INSERT_EMAIL_AUTH = "INSERT INTO email_auth (email, code, create_ts) VALUES (:email, :code, CURRENT_TIMESTAMP()) "
                                                    + "ON DUPLICATE KEY UPDATE code = :code, create_ts =  CURRENT_TIMESTAMP();";

    private static final String CHECK_AUTH = "SELECT COUNT(*) FROM email_auth WHERE email = :email AND code = :code;";                                              
    private static final String INSERT_ACCOUNT = "INSERT INTO account (account_id, email, pwd_hash, login_token, account_json)"
                                                 + " VALUES (:account_id, :email, :pwd_hash, :login_token, :account_json);";                                              
    private static final String ACCOUNT_COUNT = "SELECT COUNT(*) FROM account WHERE email = :email;";   
    private static final String TOKEN_LOGIN_CHECK = "SELECT COUNT(*) FROM account WHERE account_id = :account_id AND login_token = :login_token;";                                            
    private static final String GET_ACCOUNT_PWD_HASH = "SELECT pwd_hash FROM account WHERE email = :email;";        
    private static final String SET_ACCOUNT_LOGIN_TOKEN = "UPDATE account SET login_token = :login_token;";
    private static final String GET_ACCOUNT_INFO_BY_EMAIL = "SELECT account_json FROM account WHERE email = :email;";                                    
    private static final String GET_ACCOUNT_INFO_BY_ID = "SELECT account_json FROM account WHERE account_id = :account_id;";                                    
    @Autowired
    private BaseDao baseDao;

    public void createAuthRecord(String email, String code) {
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.EMAIL,email )
            .addValue(DaoConstants.CODE, code);
        
        jdbcTemplate.update(INSERT_EMAIL_AUTH, source);

    }
    public boolean checkAuth(String email, String code) {
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.EMAIL,email )
            .addValue(DaoConstants.CODE, code);
        int count = jdbcTemplate.queryForObject(CHECK_AUTH, source, int.class);
        return count == 1;
    }
    public LoginResponse createAccount(RegisterAccountInfo accountInfo) throws JsonProcessingException {
        // Create a login response with new accountId/loginToken
        // If a response cannot be created, i.e. email taken/invalid code then do not create account 
        // and response response with error message 
        LoginResponse response = createLoginResponse(accountInfo);
        if(!response.isSuccess()){
            return response;
        }
       
        Account account = createAccount(accountInfo, response.getAccountId());

        String accountJSON = mapper.writeValueAsString(account);
        String pwdHash = BCrypt.hashpw(accountInfo.getPassword(), BCrypt.gensalt());
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.ACCOUNT_ID, response.getAccountId())
            .addValue(DaoConstants.EMAIL, accountInfo.getEmail())
            .addValue(DaoConstants.PWD_HASH, pwdHash)
            .addValue(DaoConstants.LOGIN_TOKEN, response.getLoginToken())
            .addValue(DaoConstants.ACCOUNT_JSON, accountJSON);
        
        jdbcTemplate.update(INSERT_ACCOUNT, source);
        response.setAccount(account);
        response.setSuccess(true);

        return response;
    } 

    private LoginResponse createLoginResponse(RegisterAccountInfo accountInfo) {
        if ( accountExists(accountInfo.getEmail())){
            LoginResponse failedResponse = new LoginResponse();
            failedResponse.setSuccess(false);
            failedResponse.setErrMsg("Email already in use! Please use a different email address!");
            return failedResponse;
        }
        if (!checkAuth(accountInfo.getEmail(), accountInfo.getCode())){
            LoginResponse failedResponse = new LoginResponse();
            failedResponse.setErrMsg("Invalid Code! Please send another code to validate email again.");
            failedResponse.setSuccess(false);
            return failedResponse;
        }

        LoginResponse response = new LoginResponse();
        String accountId = baseDao.getUUID();
        String loginToken = baseDao.getUUID();
        response.setAccountId(accountId);
        response.setLoginToken(loginToken);
        response.setSuccess(true);
        return response;

    }

    private Account createAccount(RegisterAccountInfo accountInfo, String accountId){
        Account account = new Account();
        account.setAccountId(accountId);
        account.setCredits(10000);
        account.setDisplayName(accountInfo.getDisplayName());
        account.setEmail(accountInfo.getEmail());
        return account;
    }

    private boolean accountExists(String email){
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.EMAIL,email );
        int count = jdbcTemplate.queryForObject(ACCOUNT_COUNT, source, int.class);
        return count > 0;

    }

    public LoginResponse login(LoginRequest loginRequest) throws JsonProcessingException{
        boolean successfulLogin = false;   
        Account account = null;
        LoginResponse response = new LoginResponse();
        //Cached Login
        if (!StringUtils.isEmpty(loginRequest.getLoginToken())
                && !StringUtils.isEmpty(loginRequest.getAccountId())) {
            successfulLogin = checkTokenLogin(loginRequest);
            LOGGER.info("Cached login success ");
            if (!successfulLogin){
                LOGGER.info("Validated cached login session!");
                response.setErrMsg("Your session expired. Please login again.");
            } 
        } 
        //Password Login
        if (!StringUtils.isEmpty(loginRequest.getPassword())
                && !StringUtils.isEmpty(loginRequest.getEmail())) {
            LOGGER.info("Validating login credentials...");
            successfulLogin = checkPasswordLogin(loginRequest);
            if (!successfulLogin){
                LOGGER.info("Validated login credentials!");
                response.setErrMsg("Your session expired. Please login again.");
            }
        }
        if (successfulLogin) {
            response.setSuccess(true);
            account = getAccountByEmail(loginRequest.getEmail());
            
            response.setAccountId(account.getAccountId());
            String loginToken = baseDao.getUUID();
            updateLoginToken(loginToken);
            response.setLoginToken(loginToken);
            response.setAccount(account);
        } else {
            response.setSuccess(false);
        }
        LOGGER.info(response.toString());
        return response;
    }

    private boolean checkTokenLogin(LoginRequest request){
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.ACCOUNT_ID, request.getAccountId())
            .addValue(DaoConstants.LOGIN_TOKEN, request.getLoginToken());
        int count = jdbcTemplate.queryForObject(TOKEN_LOGIN_CHECK, source, int.class);
        return count == 1;
    }
    private boolean checkPasswordLogin(LoginRequest request){
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.EMAIL, request.getEmail());
        String pwdHash = jdbcTemplate.queryForObject(GET_ACCOUNT_PWD_HASH, source, String.class);
        return BCrypt.checkpw(request.getPassword(), pwdHash);
    }

    private void updateLoginToken (String loginToken) {
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.LOGIN_TOKEN, loginToken);
        jdbcTemplate.update(SET_ACCOUNT_LOGIN_TOKEN, source);
    }

    public Account getAccountById(String accountId) throws JsonProcessingException{
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.ACCOUNT_ID, accountId);
        String accountJson = jdbcTemplate.queryForObject(GET_ACCOUNT_INFO_BY_ID, source, String.class);
        return mapper.readValue(accountJson, Account.class);
    }
    public Account getAccountByEmail(String email) throws JsonProcessingException{
        SqlParameterSource source = new MapSqlParameterSource()
            .addValue(DaoConstants.EMAIL, email);
        String accountJson = jdbcTemplate.queryForObject(GET_ACCOUNT_INFO_BY_EMAIL, source, String.class);
        return mapper.readValue(accountJson, Account.class);
    }
}
