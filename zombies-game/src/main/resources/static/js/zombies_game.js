var gameId;
var playerId;
var mapId;

var game;
var map;
var gameData;

function connect() {
    gameId = localStorage.getItem("game-id");
    playerId = localStorage.getItem("player-id");

    $(document).find("#uuidInf").text("game-id: " + gameId + ", player-id: " + playerId).show();

    socket = new WebSocket("ws://localhost:8080/zombies/ws");
    socket.onopen = function(event) {
        loadGameBoard();
        sendHello(socket);
    }
    socket.onclose = function(event) {
        $(document).find("#socketInf").text("onClose: " + event).show();
    }
    socket.onmessage = function(event) {
        var data = JSON.parse(event.data);
        $(document).find("#socketInf").text("onMessage: " + data).show();
        switch(data.type) {
            case 'REQUEST':
                processGameRequest(data);
                break;
            case 'UPDATE':
//                processGameUpdate(data);
                break;
       }
    }
}

function loadGameBoard() {
    loadGame();
}

function loadGame() {
    api_games_get(
        gameId,
        function(data) {
            game = data;
            mapId = game.mapId;
            loadMap();
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#socketInf").text("onError: " + textStatus).show();
        }
    );
}

function loadMap() {
    api_maps_get(
        mapId,
        function(data) {
            map = data;
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#socketInf").text("onError: " + textStatus).show();
        }
    );
}

function loadMapTile(mapTileId) {
}

function sendHello(socket) {
    var helloMsg = '{"@type": "Hello", "playerId": "'+playerId+'", "gameId": "'+gameId+'", "type": "HELLO"}';
    $(document).find("#socketInf").text("Sending hello: " + helloMsg).show();
    //----- Fire and forget
    socket.send(helloMsg);
}

function processGameRequest(request) {
    switch(request['@type']) {
        case 'RequestRoll':
            requestRoll(request);
            break;
        case 'RequestUseBullets':
            requestUseBullets(request);
            break;
        case 'RequestMovement':
            requestMovement(request);
            break;
        case 'RequestDiscards':
            requestDiscards(request);
            break;
    }
}
function processGameUpdate(update) {
    $(document).find("#updateInf").text(JSON.stringify(update)).show();
}

function requestRoll(request) {
    var div = $(document).find("#rollDiceDiv");
    div[0].dataset.message = JSON.stringify(request);
    div[0].style.display = "block";
}
function requestMovement(request) {
    var div = $(document).find("#moveDiv");
    div[0].dataset.message = JSON.stringify(request);
    div[0].style.display = "block";
}
function requestUseBullets(request) {
    var div = $(document).find("#useBulletsDiv");
    div[0].dataset.message = JSON.stringify(request);
    div[0].style.display = "block";
}

function rollDice() {
    var div = $(document).find("#rollDiceDiv");
    div[0].style.display = "none";
    socket.send(div[0].dataset.message);
}
function useBullets(use) {
    var div = $(document).find("#useBulletsDiv");
    div[0].style.display = "block";
    var json = JSON.parse(div[0].dataset.message);
    json.usingBullets = use;
    socket.send(JSON.stringify(json));
}
function move(direction) {
    var div = $(document).find("#moveDiv");
    div[0].style.display = "block";
    var json = JSON.parse(div[0].dataset.message);
    if(direction != null) {
        json.direction = direction;
    }
    socket.send(JSON.stringify(json));
}

//    gameCanvas = document.getElementById(canvasId);
//    gameCanvas.width = gameCanvasWidth;
//    gameCanvas.height = gameCanvasHeight;
//    gameCanvas.hidden = "false";
//    gameCanvas.style.display = "block";
//    var context = gameCanvas.getContext("2d");
//    context.fillStyle = "#888888";
//    context.fillRect(0,0,gameCanvas.width,gameCanvas.height);


//    var url = baseUrl + "ws/actions";
//    gameSocket = new WebSocket(getBaseSocketUrl() + url);
//    gameSocket.onopen = onOpenFn;
//    gameSocket.onmessage = onMessageFn;
//    gameSocket.onclose = onCloseFn;