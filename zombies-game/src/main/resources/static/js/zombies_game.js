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
                processGameUpdate(data);
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
            drawMap();
            loadMapTiles();
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#socketInf").text("onError: " + textStatus).show();
        }
    );
}

function loadMapTiles() {
    for(var tl in map.mapTileIds) {
        loadMapTile(map.mapTileIds[tl]);
    }
}

function loadMapTile(mapTileId) {
    api_maptiles_get(
        mapTileId,
        function(data) {
            drawMapTile(data);
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#socketInf").text("onError: " + textStatus).show();
        }
    );
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

var SQUARE_SIZE = 45;
var OFFSET = SQUARE_SIZE/3;

function drawMap() {
    var gameCanvas = document.getElementById("gameCanvas");
    gameCanvas.width = 1800;
    gameCanvas.height = 1800;
    gameCanvas.hidden = "false";
    gameCanvas.style.display = "block";
    var context = gameCanvas.getContext("2d");
    context.fillStyle = "#888888";
    context.fillRect(0,0,gameCanvas.width,gameCanvas.height);
}

var rotmat = {
    ROT_0:   [ 0, 1, 2, 3, 4, 5, 6, 7, 8 ],
    ROT_90:  [ 6, 3, 0, 7, 4, 1, 8, 5, 2 ],
    ROT_180: [ 8, 7, 6, 5, 4, 3, 2, 1, 0 ],
    ROT_270: [ 2, 5, 8, 1, 4, 7, 0, 3, 6 ]
};

function getX(x) {
    return (x - map.minx) * SQUARE_SIZE;
}
function getY(y) {
    return (y - map.miny) * SQUARE_SIZE;
}

function drawMapTile(mapTile) {
    var gameCanvas = document.getElementById("gameCanvas");
    var context = gameCanvas.getContext("2d");

    var tl = mapTile.topLeft;
    var x = getX(tl.x);
    var y = getY(tl.y);
    for(var i = 0; i < 3; i++) {
        for(var j = 0; j < 3; j++) {
            var sqType = mapTile.tile.squareTypes[rotmat[mapTile.rotation][i + j*3]];
            context.fillStyle = getFill(sqType);
            context.fillRect(x + i*SQUARE_SIZE, y + j*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
    }
    drawBullets();
    drawLife();
    drawZombies();
}

var font = "bold 30px Arial";

function drawBullets() {
    var gameCanvas = document.getElementById("gameCanvas");
    var context = gameCanvas.getContext("2d");
    context.font = font;
    context.fillStyle = '#000000';
    for(var i = 0; i < game.bulletLocations.length; i++) {
        var loc = game.bulletLocations[i];
        context.fillText('⦿', getX(loc.x) + OFFSET, getY(loc.y) + SQUARE_SIZE - OFFSET, SQUARE_SIZE);
    }
}
function drawLife() {
    var gameCanvas = document.getElementById("gameCanvas");
    var context = gameCanvas.getContext("2d");
    context.font = font;
    context.fillStyle = '#FF0000';
    context.strokeStyle = '#000000';
    for(var i = 0; i < game.lifeLocations.length; i++) {
        var loc = game.lifeLocations[i];
        context.strokeText('♥', getX(loc.x) + OFFSET, getY(loc.y) + SQUARE_SIZE - OFFSET, SQUARE_SIZE);
        context.fillText('♥', getX(loc.x) + OFFSET, getY(loc.y) + SQUARE_SIZE - OFFSET, SQUARE_SIZE);
    }
}
function drawZombies() {
    var gameCanvas = document.getElementById("gameCanvas");
    var context = gameCanvas.getContext("2d");
    context.font = font;
    context.fillStyle = '#FFFF00';
    context.strokeStyle = '#000000';
    for(var i = 0; i < game.zombieLocations.length; i++) {
        var loc = game.zombieLocations[i];
        context.strokeText('Z', getX(loc.x) + OFFSET, getY(loc.y) + SQUARE_SIZE - OFFSET, SQUARE_SIZE);
        context.fillText('Z', getX(loc.x) + OFFSET, getY(loc.y) + SQUARE_SIZE - OFFSET, SQUARE_SIZE);
    }
}

function getFill(sqType) {
    switch(sqType) {
        case 'IMPASSABLE':
            return '#000000';
        case 'ROAD':
            return '#FFFFFF';
        case 'BUILDING':
            return '#777777';
        case 'DOOR':
            return '#964B00';
        case 'HELICOPTER':
            return '#00aa00';
        case 'TOWN_SQUARE':
            return '#0000aa';
    }
}