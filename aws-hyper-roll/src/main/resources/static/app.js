var currentScreen = 'HOME';
	
var gameState;
var lobbyIds;

var numberOfPolls = 0;

function createLobby(){
    name = $('#create-lobby-name').val().trim();
    var lobby = {
                    'player':{'name': name},
                    'initRoll': $('#create-lobby-starting-roll').val(),
                    'botGame': false
                };
    
    $.ajax({
        type:'POST',
        url:'/lobby',
        data:JSON.stringify(lobby),
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        success: function(data){startLobby(data);},
        error: function(errMsg) {alert('Something broke :(');}
    });


}

function joinLobby(){
    name = $('#join-lobby-name').val().trim();
    var player = {
        'name': name
    };
    $.ajax({
        type:'PATCH',
        url:'/lobby/join/' + $('#join-lobby-code').val(),
        data:JSON.stringify(player),
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        success: function(data){startLobby(data);},
        error: function(errMsg) {alert('Something broke :(');}
    });
}
function startLobby(data){
    currentScreen = 'GAME';
    $('#game').show();
    $('.lobby').hide();
    $('#player-game-name').html(name);
    lobbyIds = data;
    pollGame(lobbyIds.gameId, lobbyIds.playerId);
}

function pollGame(gameId, playerId){
    $.ajax({
        type:'GET',
        url:'/game/' + gameId + '/player/' + playerId,
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        success: function(data) {
                            updateGameStatus(data);
                            if(numberOfPolls > 60 ) {
                                alert('Game timed out after ten minutes!');
                                console.log('Timed Out');
                            } else if( gameState.gameStatus !== 'FINISHED' && currentScreen === 'GAME'){
                                numberOfPolls++;
                                pollGame(gameId, playerId);
                            } else {
                                console.log('Game finised or aborted');
                            }

                        },
        error: function(errMsg) {alert('Something broke :(');}
    });
}

function roll (){
    $.ajax({
        type:'PATCH',
        url:'/game/roll/' + lobbyIds.gameId,
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        success: function(data){updateGameStatus(data);},
        error: function(errMsg) {alert('Something broke :(');}
    });
}

function updateGameStatus (data){
    gameState = data;
    var gameLogInput = $('#game-log');
    gameLogInput.val(gameState.gameLogString);
    gameLogInput.scrollTop(gameLogInput[0].scrollHeight);
    // Change button text
    if(gameState.gameStatus === 'FINISHED') {
        $('#roll-button').html('GAME OVER');
        $('#roll-button').attr('disabled', true);


    } else {
        $('#roll-button').html('Player ' + gameState.players[0].name + '\'s roll!');
        $('#roll-button').attr('disabled', lobbyIds.playerId !== gameState.players[0].playerId);
    }
}

function botGameBtnClick(){
    currentScreen = 'GAME';
    $('#game').show();
    $('#home-menu').hide();
}
function createLobbyBtnClick(){
    currentScreen = 'CREATE_LOBBY';
    $('#create-lobby').show();
    $('#home-menu').hide();
}
function joinLobbyBtnClick(){
    currentScreen = 'JOIN_LOBBY';
    $('#join-lobby').show();
    $('#home-menu').hide();
}
function homeBtnClick(){
    currentScreen = 'HOME';
    numberOfPolls = 0;
    $('#home-menu').show();		
    $('.game-container').hide();
}
