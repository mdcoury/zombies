package ca.adaptor.zombies.game.model;

public class ZombiesModelConstants {
    public static final String TABLE_GAME_DATA = "game_data";
    public static final String TABLE_EVENT_CARD = "event_card";
    public static final String TABLE_GAME = "game";
    public static final String TABLE_MAP = "map";
    public static final String TABLE_MAP_TILE = "map_tile";
    public static final String TABLE_PLAYER = "player";
    public static final String TABLE_TILE = "tile";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TILE_ID = TABLE_TILE + "_" + COLUMN_ID;
    public static final String COLUMN_MAP_ID = TABLE_MAP + "_" + COLUMN_ID;

    public static final String COLUMN_MAP_TILES = "map_tiles";

    public static final String COLUMN_X = "x";
    public static final String COLUMN_Y = "y";

    public static final String COLUMN_TOWN_SQUARE_X = "town_square_x";
    public static final String COLUMN_TOWN_SQUARE_Y = "town_square_y";
    public static final String COLUMN_HELIPAD_X = "helipad_x";
    public static final String COLUMN_HELIPAD_Y = "helipad_y";

    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_CARD_IDS = "card_ids";
    public static final String COLUMN_NUM_BULLETS = "num_bullets";
    public static final String COLUMN_NUM_LIFE = "num_life";
    public static final String COLUMN_NUM_ZOMBIES = "num_zombies";

    public static final String COLUMN_BULLETS_XY = "bullets_xy";
    public static final String COLUMN_LIFES_XY = "lifes_xy";
    public static final String COLUMN_ZOMBIES_XY = "zombies_xy";
    public static final String COLUMN_GAME_DATA = "game_data";
    public static final String COLUMN_POPULATED = "populated";
    public static final String COLUMN_RUNNING = "running";
    public static final String COLUMN_TURN = "turn";

    public static final String COLUMN_ROTATION = "rotation";
    public static final String COLUMN_TOP_LEFT = "top_left";

    public static final String COLUMN_SQUARE_TYPES = "square_types";
    public static final String COLUMN_NAME = "name";

    public static final String INDEX_TILE_NAME = "idx_tile_name";
}
