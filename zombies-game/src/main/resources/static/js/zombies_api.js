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

function api_games_init_put(gameId, successFn, errorFn, async = true) {
    var url = baseUrl + "games/" + gameId + "/init";
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