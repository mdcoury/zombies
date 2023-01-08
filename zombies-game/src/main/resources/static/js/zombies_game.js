var gameId;
var playerId;

function connect() {
    gameId = localStorage.getItem("game-id");
    playerId = localStorage.getItem("player-id");

    socket = new WebSocket("ws://localhost:8080/zombies/ws");
    socket.onopen = function(event) {
//        $(document).find("#socketTest").text("Sending hello...").show();
        sendHello(socket);
    }
    socket.onclose = function(event) {
//        $(document).find("#socketTest").text("Socket closed.").show();
    }
    socket.onmessage = function(event) {
        var data = JSON.parse(event.data);
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

function sendHello(socket) {
    socket.send(
        '{"@type": "Hello", "playerId": "'+playerId+'", "gameId": "'+gameId+'", "type": "HELLO"}'
    );
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