package com.api.awshyperroll.dao;

public interface DaoConstants {
    // SQL Constants 
    
    // Game columns
    public static final String GAME_ID = "game_id";
    public static final String GAME_JSON = "game_json";
    public static final String ROLL = "roll";
    public static final String PLAYER_NM = "player_nm";

    // lobby columns
    public static final String LOBBY_ID = "lobby_id";
    public static final String CODE = "code";
    public static final String LOBBY_JSON = "lobby_json";
    public static final String ACTV_FLAG = "actv_flag";

    //player columns 
    public static final String PLAYER_ID = "player_id";
    public static final String PLAYER_JSON = "player_json";
    public static final String HAS_LATEST_GAME = "has_latest_game";

    //account columns 
    public static final String EMAIL = "email";
    
}
