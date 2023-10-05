let currentScreen = 'LOGIN';
let playerName;	
let gameState;
let lobbyIds;
let guestFlag;

let pollTimeoutLimit = 0;

function sendCode(){
    const email = $('#email').val();
    const emailDetails = {
        recipient:email
    }
    $.ajax({
        type:'POST',
        url:'/account/verifyEmail',
        data:JSON.stringify(emailDetails),
        contentType: 'application/json; charset=utf-8',
        success: function(data){alert('Code Sent!')},
        error: function(errMsg) {alert('Something broke :(');}
    })
}

function register(){
    const registerAccountInfo = {
                    'email': $('#email').val(),
                    'password':$('#password').val(),
                    'displayName':$('#reg-name').val(),
                    'code': $('#verify-code').val()
                };
    
    $.ajax({
        type:'POST',
        url:'/account',
        data:JSON.stringify(registerAccountInfo),
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        success: function(data){loginCallback(data);},
        error: function(errMsg) {alert('Something broke :(');}
    });
}

function login(){
    login(null);
}
// Finish login with token Start login with code.
function login(loginCache){
    let loginRequestInfo;
    if (loginCache) {
       loginRequestInfo = loginCache;
    } else {
        loginRequestInfo = {
            'email': $('#email').val(),
            'password':$('#password').val(),
        };
    }
   
    $.ajax({
        type:'POST',
        url:'/account/login',
        data:JSON.stringify(loginRequestInfo),
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        success: function(data){loginCallback(data);},
        error: function(errMsg) {alert('Something broke :(');}
    });
}

function createLobby(){
    playerName = $('#create-lobby-name').val().trim();
    const lobby = {
                    'players':[{'name': playerName}],
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
    playerName = $('#join-lobby-name').val().trim();
    const player = {
        'name': playerName
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
    $('#player-game-name').html(playerName);
    lobbyIds = data;
    pollLobby(lobbyIds.lobbyId, lobbyIds.playerId);
}

function pollLobby(lobbyId, playerId){
    $.ajax({
        type:'GET',
        url:'/lobby/' + lobbyId + '/player/' + playerId,
        contentType: 'application/json; charset=utf-8',
        dataType: 'json',
        success: function(data) {
                            updateGameStatus(data);
                            if(pollTimeoutLimit > 60 ) {
                                alert('Game timed out after ten minutes!');
                                console.log('Timed Out');
                            } else if( currentScreen === 'GAME'){
                                pollTimeoutLimit++;
                                pollLobby(lobbyId, playerId);
                            } else {
                                console.log('Game finished or aborted');
                            }

                        },
        error: function(errMsg) {alert('Something broke :(');}
    });
}

function roll (){
    pollTimeoutLimit = 0;
    $.ajax({
        type:'PATCH',
        url:'/game/roll/' + lobbyIds.gameId,
        contentType: 'application/json; charset=utf-8',
        success: function(data){console.log('Roll Successful')},
        error: function(errMsg) {alert('Something broke :(');}
    });
}

function sendMessage (){
    const gameMessage = {
        gameId:lobbyIds.gameId,
        playerId:lobbyIds.playerId,
        message: $('#message').val()
    }

    $.ajax({
        type:'POST',
        url:'/game/message',
        data:JSON.stringify(gameMessage),
        contentType: 'application/json; charset=utf-8',
        success: function(data){console.log('Message Successful'); $('#message').val('')},
        error: function(errMsg) {alert('Something broke :(');}
    });
}

function playAgain(){
    $.ajax({
        type:'PATCH',
        url:'/lobby/reset' ,
        data:JSON.stringify(lobbyIds),
        contentType: 'application/json; charset=utf-8',
        success: function() { console.log("Reset Successful") },
        error: function(errMsg) { 
                    alert('Something broke :(');
                    console.error(errMsg) ;
            }
    });
}

function leaveLobby(){
    $.ajax({
        type:'POST',
        url:'/lobby/' + lobbyIds.lobbyId + '/player/' + lobbyIds.playerId + '/leave',
        success: function() { console.log("Reset Successful"); },
        error:  function (errMsg) { 
                    alert('Something broke :(');
                    console.error(errMsg) ;
                }
    });
}


function updateGameStatus (data){
    gameState = data.game;
    lobbyIds = data.lobbyIds
    const gameLogInput = $('#game-log');
    gameLogInput.val(gameState.gameLogString);
    gameLogInput.scrollTop(gameLogInput[0].scrollHeight);
    // Change button text
    if(gameState.gameStatus === 'FINISHED') {
        $('#roll-button').html('GAME OVER');
        $('#roll-button').attr('disabled', true);
        $('#play-again-button').show();

    } else {
        $('#roll-button').html('Player ' + gameState.players[0].name + '\'s roll!');
        $('#roll-button').attr('disabled', lobbyIds.playerId !== gameState.players[0].playerId);
        $('#play-again-button').hide();

    }
}

function loginCallback(data){
    const loginResponse = data;
    if (!loginResponse.success) {
        alert(data.errMsg);
        loginBackBtn();
        localStorage.clear();
        return;
    }
    if (typeof(Storage) !== 'undefined') {
        localStorage.setItem ('accountId', loginResponse.accountId);
        localStorage.setItem ('loginToken', loginResponse.loginToken);
        
    } else {
        alert("Sorry! You have no web storage support and your login will not be cached :(");
    }
    $('#home-menu').show();	
    $('#register-form').hide();
    $('#login-screen').hide();
    $('#login-register-form').hide();
    $('#player-display-name').text(data.account.displayName);
    $('#account-credits').text(data.account.credits);

    currentScreen = 'HOME';

}

function loginMenuClick(){
    // if loginToken and Account are cached call login immediately
    if (localStorage.getItem('accountId')
            && localStorage.getItem('loginToken')) {
        let loginRequestInfo = {
            'accountId': localStorage.getItem('accountId'),
            'loginToken': localStorage.getItem('loginToken'),
        }
        login(loginRequestInfo);
    } else {
        $('#login-register-form').show();
        $('#login-form').show();
        $('#login-screen').hide();
     }

}

function registerMenuCLick(){
    $('#login-register-form').show();
    $('#register-form').show();
    $('#login-screen').hide();
}

function loginBackBtn(){
    $('#login-screen').show();
    $('#login-register-form').hide();
    $('#register-form').hide();
    $('#login-form').hide();


}
function guestClick(){
    $('#home-menu').show();	
    $('#login-screen').hide();
    currentScreen = 'HOME';

}
function botGameBtnClick(){
    $('#game').show();
    $('#home-menu').hide();
    currentScreen = 'GAME';

}
function createLobbyBtnClick(){
    $('#create-lobby').show();
    $('#game-screens').show();
    $('#home-menu').hide();
    currentScreen = 'CREATE_LOBBY';

}
function joinLobbyBtnClick(){
    $('#join-lobby').show();
    $('#game-screens').show();
    $('#home-menu').hide();    
    currentScreen = 'JOIN_LOBBY';

}
function homeBtnClick(){
    if (currentScreen === 'GAME'){
        leaveLobby();
    }
    pollTimeoutLimit = 0;
    $('#home-menu').show();		
    $('.game-container').hide();
    $('#game-screens').hide();
    currentScreen = 'HOME';   
}

function logout(){
    $('#home-menu').hide();		
    $('#login-screen').show();
    localStorage.clear();
    currentScreen = 'LOGIN';

}
