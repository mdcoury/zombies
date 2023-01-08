function soloGame() {
    createPlayer(
        function() {
            createGame(
                function() {
                    joinGame(
                        function() {
                            populateGame(
                                function() {
                                    startGame();
                                }
                            )
                        }
                    )
                }
            )
        }
    );
}
function createPlayer(successFn) {
    api_players_post(
        function(data) {
            $(document).find("#newPlayerId").text(data).show();
            if(successFn) {
                successFn();
            }
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

function createGame(successFn) {
    api_games_post(
        function(data) {
            $(document).find("#newGameId").text(data).show();
            if(successFn) {
                successFn();
            }
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#newGameId").text("Failed to create new game.").show();
        }
    );
}

function joinGame(successFn) {
    api_games_join_put(
        $(document).find("#newGameId").text(),
        $(document).find("#newPlayerId").text(),
        function(data) {
            $(document).find("#joinGameResult").text(data).show();
            if(successFn) {
                successFn();
            }
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#joinGameResult").text("Failed to join game.").show();
        }
    );
}

function populateGame(successFn) {
    api_games_populate_put(
        $(document).find("#newGameId").text(),
        function(data) {
            $(document).find("#initGameResult").text(data).show();
            if(successFn) {
                successFn();
            }
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
            localStorage.setItem("game-id", $(document).find("#newGameId").text());
            localStorage.setItem("player-id", $(document).find("#newPlayerId").text());
            location.href = "board.html";
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#startGameResult").text("Failed to start game.").show();
        }
    );
}
