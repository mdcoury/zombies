package ca.adaptor.zombies.game.model;

public class ZombiesModelConstants {
    public static final String TABLE_GAME_DATA                                                  = "game_data";
    public static final String TABLE_EVENT_CARD                                                 = "event_card";
    public static final String TABLE_GAME                                                       = "game";
    public static final String TABLE_MAP                                                        = "map";
    public static final String TABLE_MAP_TILE                                                   = "map_tile";
    public static final String TABLE_PLAYER                                                     = "player";
    public static final String TABLE_TILE                                                       = "tile";

    public static final String COLUMN_MAP_ID                                                    = "map_id";
    public static final String COLUMN_MAP_TILE_ID                                               = "map_tile_id";
    public static final String COLUMN_TILE_ID                                                   = "tile_id";
    public static final String COLUMN_GAME_ID                                                   = "game_id";
    public static final String COLUMN_GAME_PLAYER_IDS                                           = "player_ids";
    public static final String COLUMN_GAME_DATA_ID                                              = "data_id";
    public static final String COLUMN_PLAYER_ID                                                 = "player_id";
    public static final String COLUMN_EVENT_CARD_ID                                             = "event_card_id";

    public static final String COLUMN_MAP_MAP_TILES                                             = "map_map_tiles";

    public static final String COLUMN_X                                                         = "x";
    public static final String COLUMN_Y                                                         = "y";

    public static final String COLUMN_TOWN_SQUARE_X                                             = "town_square_x";
    public static final String COLUMN_TOWN_SQUARE_Y                                             = "town_square_y";
    public static final String COLUMN_HELIPAD_X                                                 = "helipad_x";
    public static final String COLUMN_HELIPAD_Y                                                 = "helipad_y";

    public static final String COLUMN_GAME_DATA_LOCATION                                        = "game_data_location";
    public static final String COLUMN_GAME_DATA_CARDS                                           = "game_data_cards";
    public static final String COLUMN_GAME_DATA_NUM_BULLETS                                     = "game_data_bullets";
    public static final String COLUMN_GAME_DATA_NUM_LIFE                                        = "game_data_life";

    public static final String COLUMN_GAME_BULLETS_XY                                           = "game_bullets_xy";
    public static final String COLUMN_GAME_LIFES_XY                                             = "game_lifes_xy";
    public static final String COLUMN_GAME_ZOMBIES_XY                                           = "game_zombies_xy";
    public static final String COLUMN_GAME_GAME_DATA                                            = "game_game_data";
    public static final String COLUMN_GAME_INITIALIZED                                          = "game_initialized";
    public static final String COLUMN_GAME_TURN                                                 = "game_turn";

    public static final String COLUMN_MAP_TILE_ROTATION                                         = "map_tile_rotation";
    public static final String COLUMN_MAP_TILE_TOP_LEFT                                         = "map_tile_top_left";

    public static final String COLUMN_TILE_SQUARE_TYPES                                         = "tile_square_types";
    public static final String COLUMN_TILE_NUM_ZOMBIES                                          = "tile_num_zombies";
    public static final String COLUMN_TILE_NUM_LIFE                                             = "tile_num_life";
    public static final String COLUMN_TILE_NUM_BULLETS                                          = "tile_num_bullets";
    public static final String COLUMN_TILE_NAME                                                 = "tile_name";

    public static final String INDEX_TILE_NAME                                                  = "idx_tile_name";
}
