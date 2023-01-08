function soloGame() {
    api_players_post(
        function(data) {
            $(document).find("#newPlayerId").text(data).show();
            api_games_post(
                function(data) {
                    $(document).find("#newGameId").text(data).show();
                    api_games_join_put(
                        $(document).find("#newGameId").text(),
                        $(document).find("#newPlayerId").text(),
                        function(data) {
                            $(document).find("#joinGameResult").text(data).show();
                            api_games_init_put(
                                $(document).find("#newGameId").text(),
                                function(data) {
                                    $(document).find("#initGameResult").text(data).show();
                                    api_games_start_put(
                                        $(document).find("#newGameId").text(),
                                        function(data) {
                                            $(document).find("#startGameResult").text(data).show();
                                            socket = new WebSocket("ws://localhost:8080/zombies/ws");
                                            socket.onopen = function(event) {
                                                $(document).find("#socketTest").text("fart").show();
                                            }
                                            socket.onclose = function(event) {
                                                $(document).find("#socketTest").text("breath").show();
                                            }
                                        },
                                        function(jqXHR, textStatus, errorThrown) {
                                            $(document).find("#startGameResult").text("Failed to start game.").show();
                                        }
                                    );
                                },
                                function(jqXHR, textStatus, errorThrown) {
                                    $(document).find("#initGameResult").text("Failed to initialize game.").show();
                                }
                            );
                        },
                        function(jqXHR, textStatus, errorThrown) {
                            $(document).find("#joinGameResult").text("Failed to join game.").show();
                        }
                    );
                },
                function(jqXHR, textStatus, errorThrown) {
                    $(document).find("#newGameId").text("Failed to create new game.").show();
                }
            );
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#newPlayerId").text("Failed to create new player.").show();
        }
    );
}
function createPlayer() {
    api_players_post(
        function(data) {
            $(document).find("#newPlayerId").text(data).show();
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#newPlayerId").text("Failed to create new player.").show();
        }
    );
}

function createMap() {
    api_maps_post(
        function(data) {
            $(document).find("#newMapId").text(data).show();
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#newMapId").text("Failed to create new map.").show();
        }
    );
}

function createGame() {
    api_games_post(
        function(data) {
            $(document).find("#newGameId").text(data).show();
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#newGameId").text("Failed to create new game.").show();
        }
    );
}

function joinGame() {
    api_games_join_put(
        $(document).find("#newGameId").text(),
        $(document).find("#newPlayerId").text(),
        function(data) {
            $(document).find("#joinGameResult").text(data).show();
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#joinGameResult").text("Failed to join game.").show();
        }
    );
}

function populateGame() {
    api_games_populate_put(
        $(document).find("#newGameId").text(),
        function(data) {
            $(document).find("#initGameResult").text(data).show();
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#initGameResult").text("Failed to initialize game.").show();
        }
    );
}

function startGame() {
    api_games_start_put(
        $(document).find("#newGameId").text(),
        function(data) {
            $(document).find("#startGameResult").text(data).show();
            socket = new WebSocket("ws://localhost:8080/zombies/ws");
            socket.onopen = function(event) {
                $(document).find("#socketTest").text("Sending hello...").show();
                sendHello(socket);
            }
            socket.onclose = function(event) {
                $(document).find("#socketTest").text("Socket closed.").show();
            }
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#startGameResult").text("Failed to start game.").show();
        }
    );
}

function sendHello(socket) {
    gameId = $(document).find("#newGameId").text();
    playerId = $(document).find("#newPlayerId").text();
    socket.send(
        '"HelloMessage": { "playerId": "'+playerId+'", "gameId": "'+gameId+'", "type": "HELLO" }'
    );
}