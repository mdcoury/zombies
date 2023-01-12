var gameId;
var playerId;
var mapId;

var game;
var map;
var mapTiles = new Array();
var gameData;
var bulletLocations;
var lifeLocations;
var zombieLocations;

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
            bulletLocations = game.bulletLocations;
            lifeLocations = game.lifeLocations;
            zombieLocations = game.zombieLocations;
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
            initGameCanvas();
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
            mapTiles.push(data);
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
    redrawBoard();
}
function processGameUpdate(update) {
    if(update.turn && update.phase) {
        $(document).find("#gameTurnInf").text("Turn: " + update.turn + ", Phase: " + update.phase).show();
    }

    if(update.zombieMovements || update.zombieKills) {
        updateZombieLocations(update);
    }
    if(update.roll) {
        $(document).find("#rollInf").text("Roll=" + update.roll).show();
    }
    if(update.playerData) {
        gameData = update.playerData;
        drawPlayerData();
    }
}
function moveZombie(src, dest) {
    var found = false;
    for(var i = 0; i < zombieLocations.length && !found; i++) {
        var loc = zombieLocations[i];
        if(loc.x == src.x && loc.y == src.y) {
            found = true;
            zombieLocations[i] = dest;
        }
    }
}
function removeZombie(src) {
    var found = false;
    for(var i = 0; i < zombieLocations.length && !found; i++) {
        var loc = zombieLocations[i];
        if(loc.x == src.x && loc.y == src.y) {
            found = true;
            zombieLocations.splice(i,1);
        }
    }
}
function updateBulletLocations(update) {
    redrawBoard();
}
function updateLifeLocations(update) {
    redrawBoard();
}
function updateZombieLocations(update) {
    if(update.zombieMovements) {
        var keys = Object.keys(update.zombieMovements);
        for(var i = 0; i < keys.length; i++) {
            var key = keys[i];
            var src = JSON.parse(key);
            var dest = update.zombieMovements[key];
            moveZombie(src, dest);
        }
    }
    if(update.zombieKills) {
        for(var i = 0; i < update.zombieKills.length; i++) {
            removeZombie(update.zombieKills[i]);
        }
    }
    redrawBoard();
}

function requestRoll(request) {
    $(document).find("#rollInf").text("Roll=").show();
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
    if(div[0].dataset.message) {
        var json = JSON.parse(div[0].dataset.message);
        json.type = "REPLY";
        socket.send(JSON.stringify(json));
    }
    div[0].dataset.message = null;
}
function useBullets(use) {
    var div = $(document).find("#useBulletsDiv");
    div[0].style.display = "none";
    if(div[0].dataset.message) {
        var json = JSON.parse(div[0].dataset.message);
        json.type = "REPLY";
        json.usingBullets = use;
        socket.send(JSON.stringify(json));
    }
    div[0].dataset.message = null;
}
function move(direction) {
    var div = $(document).find("#moveDiv");
    div[0].style.display = "none";
    if(div[0].dataset.message) {
        var json = JSON.parse(div[0].dataset.message);
        json.type = "REPLY";
        if(direction != null) {
            json.direction = direction;
        }
        socket.send(JSON.stringify(json));
    }
    div[0].dataset.message = null;
}

var SQUARE_SIZE = 24;
var OFFSET = SQUARE_SIZE/3;
var FONT = "bold "+(SQUARE_SIZE-OFFSET)+"px Arial";

function redrawBoard() {
//    initGameCanvas();
    for(var i = 0; i < mapTiles.length; i++) {
        drawMapTile(mapTiles[i]);
    }
    drawBullets();
    drawLife();
    drawZombies();
    drawPlayer();
}

function initGameCanvas() {
    var gameCanvas = document.getElementById("gameCanvas");
    gameCanvas.width = 1800;
    gameCanvas.height = 1800;
    gameCanvas.hidden = "false";
    gameCanvas.style.display = "block";
    var context = gameCanvas.getContext("2d");
    context.fillStyle = "#888888";
    context.fillRect(0,0,gameCanvas.width,gameCanvas.height);
}

function getX(x) { return (x - map.minx) * SQUARE_SIZE; }
function getY(y) { return (y - map.miny) * SQUARE_SIZE; }

function drawPlayerData() {
    $(document).find("#playerDataInf").text(
        "Location = (" + gameData.location.x + "," + gameData.location.y + ")"
        + ", # Bullets = " + gameData.numBullets
        + ", # Life = " + gameData.numLife
    ).show();
}

function drawMapTile(mapTile) {
    var gameCanvas = document.getElementById("gameCanvas");
    var context = gameCanvas.getContext("2d");

    var tl = mapTile.topLeft;
    var x = getX(tl.x);
    var y = getY(tl.y);
    for(var i = 0; i < 3; i++) {
        for(var j = 0; j < 3; j++) {
            var sqType = mapTile.squareTypes[i + j*3];
            context.fillStyle = getFill(sqType);
            context.fillRect(x + i*SQUARE_SIZE, y + j*SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
        }
    }
    drawBullets();
    drawLife();
    drawZombies();
}

function drawPlayer() {
    var gameCanvas = document.getElementById("gameCanvas");
    var context = gameCanvas.getContext("2d");
    context.font = FONT;
    context.fillStyle = '#000000';
    context.strokeStyle = '#ffffff';
    context.strokeText('P', getX(gameData.location.x) + OFFSET, getY(gameData.location.y) + SQUARE_SIZE - OFFSET, SQUARE_SIZE)
    context.fillText('P', getX(gameData.location.x) + OFFSET, getY(gameData.location.y) + SQUARE_SIZE - OFFSET, SQUARE_SIZE)
}
function drawBullets() {
    var gameCanvas = document.getElementById("gameCanvas");
    var context = gameCanvas.getContext("2d");
    context.font = FONT;
    context.fillStyle = '#000000';
    for(var i = 0; i < bulletLocations.length; i++) {
        var loc = bulletLocations[i];
        context.fillText('⦿', getX(loc.x) + OFFSET, getY(loc.y) + SQUARE_SIZE - OFFSET, SQUARE_SIZE);
    }
}
function drawLife() {
    var gameCanvas = document.getElementById("gameCanvas");
    var context = gameCanvas.getContext("2d");
    context.font = FONT;
    context.fillStyle = '#FF0000';
    context.strokeStyle = '#000000';
    for(var i = 0; i < lifeLocations.length; i++) {
        var loc = lifeLocations[i];
        context.strokeText('♥', getX(loc.x) + OFFSET, getY(loc.y) + SQUARE_SIZE - OFFSET, SQUARE_SIZE);
        context.fillText('♥', getX(loc.x) + OFFSET, getY(loc.y) + SQUARE_SIZE - OFFSET, SQUARE_SIZE);
    }
}
function drawZombies() {
    var gameCanvas = document.getElementById("gameCanvas");
    var context = gameCanvas.getContext("2d");
    context.font = FONT;
    context.fillStyle = '#FFFF00';
    context.strokeStyle = '#000000';
    for(var i = 0; i < zombieLocations.length; i++) {
        var loc = zombieLocations[i];
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