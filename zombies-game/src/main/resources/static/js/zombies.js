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

function initGame() {
    api_games_init_put(
        $(document).find("#newGameId").text(),
        function(data) {
            $(document).find("#initGameResult").text(data).show();
        },
        function(jqXHR, textStatus, errorThrown) {
            $(document).find("#initGameResult").text("Failed to initialize game.").show();
        }
    );
}