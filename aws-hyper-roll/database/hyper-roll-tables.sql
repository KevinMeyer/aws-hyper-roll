
CREATE TABLE roll_hist (
  roll_hist_id int(11) NOT NULL AUTO_INCREMENT,
  roll int(11) DEFAULT NULL,
  player_nm varchar(255) DEFAULT NULL,
  PRIMARY KEY (roll_hist_id)
);

CREATE TABLE game (
	game_id VARCHAR(36) NOT NULL,
	game_json json NOT NULL,
	PRIMARY KEY (game_id)
);

CREATE TABLE lobby (
	lobby_id VARCHAR(36) NOT NULL,
	code VARCHAR(4) NOT NULL,
	game_id VARCHAR(36) NOT NULL,
	lobby_json json NOT NULL,
	upd_ts TIMESTAMP NOT NULL,
	actv_flag boolean NOT NULL DEFAULT 1,
	PRIMARY KEY (lobby_id),
	FOREIGN KEY (game_id)
		REFERENCES game(game_id)
		ON DELETE CASCADE
);

ALTER TABLE lobby ADD INDEX lobby_code_index (code);


CREATE TABLE player (
	player_id VARCHAR(36) NOT NULL,
	lobby_id VARCHAR(36) NOT NULL,
	has_latest_game boolean NOT NULL DEFAULT 0,
	player_json json NOT NULL,
	PRIMARY KEY (player_id),
	FOREIGN KEY (lobby_id)
		REFERENCES lobby(lobby_id)
		ON DELETE CASCADE
);


