var baseUrl = "/zombies/";

function api_players_post(successFn, errorFn, async = true) {
    var url = baseUrl + "players";
    var request = new Object();

    jQuery.ajax({
        url: url,
        data: JSON.stringify(request),
        contentType: 'application/json',
        datatype: 'json',
        context: document.body,
        type: 'POST',
        async: async,
        headers: {
        },
        success: successFn,
        error: errorFn
    });
}

function api_maps_post(successFn, errorFn, async = true) {
    var url = baseUrl + "maps";
    var request = new Object();

    jQuery.ajax({
        url: url,
        data: JSON.stringify(request),
        contentType: 'application/json',
        datatype: 'json',
        context: document.body,
        type: 'POST',
        async: async,
        headers: {
        },
        success: successFn,
        error: errorFn
    });
}

function api_maps_get(mapId, successFn, errorFn, async = true) {
    var url = baseUrl + "maps/" + mapId;

    jQuery.ajax({
        url: url,
        context: document.body,
        type: 'GET',
        async: async,
        headers: {
        },
        success: successFn,
        error: errorFn
    });
}

function api_maptiles_get(mapTileId, successFn, errorFn, async = true) {
    var url = baseUrl + "maptiles/" + mapTileId;

    jQuery.ajax({
        url: url,
        context: document.body,
        type: 'GET',
        async: async,
        headers: {
        },
        success: successFn,
        error: errorFn
    });
}

function api_games_post(successFn, errorFn, async = true) {
    var url = baseUrl + "games";
    var request = new Object();

    jQuery.ajax({
        url: url,
        data: JSON.stringify(request),
        contentType: 'application/json',
        datatype: 'json',
        context: document.body,
        type: 'POST',
        async: async,
        headers: {
        },
        success: successFn,
        error: errorFn
    });
}

function api_games_get(gameId, successFn, errorFn, async = true) {
    var url = baseUrl + "games/" + gameId;

    jQuery.ajax({
        url: url,
        context: document.body,
        type: 'GET',
        async: async,
        headers: {
        },
        success: successFn,
        error: errorFn
    });
}

function api_games_join_put(gameId, playerId, successFn, errorFn, async = true) {
    var url = baseUrl + "games/" + gameId + "/join/" + playerId;
    var request = new Object();

    jQuery.ajax({
        url: url,
        data: JSON.stringify(request),
        contentType: 'application/json',
        datatype: 'json',
        context: document.body,
        type: 'PUT',
        async: async,
        headers: {
        },
        success: successFn,
        error: errorFn
    });
}

function api_games_populate_put(gameId, successFn, errorFn, async = true) {
    var url = baseUrl + "games/" + gameId + "/populate";
    var request = new Object();

    jQuery.ajax({
        url: url,
        data: JSON.stringify(request),
        contentType: 'application/json',
        datatype: 'json',
        context: document.body,
        type: 'PUT',
        async: async,
        headers: {
        },
        success: successFn,
        error: errorFn
    });
}

function api_games_start_put(gameId, successFn, errorFn, async = true) {
    var url = baseUrl + "games/" + gameId + "/start";
    var request = new Object();

    jQuery.ajax({
        url: url,
        data: JSON.stringify(request),
        contentType: 'application/json',
        datatype: 'json',
        context: document.body,
        type: 'PUT',
        async: async,
        headers: {
        },
        success: successFn,
        error: errorFn
    });
}