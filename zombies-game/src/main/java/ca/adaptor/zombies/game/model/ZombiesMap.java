package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;

@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table
public class ZombiesMap {
    public enum Tile {
        WATER(' ',32),
        LAND('.',1),
        ;

        private final char tile;
        @Getter
        private final int weight;

        Tile(char tile, int weight) {
            this.tile = tile;
            this.weight = weight;
        }
    }

    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    @Embeddable
    static class Coordinate {
        int x;
        int y;
    }

    @Getter
    @Id
    @Column(name = COLUMN_MAP_ID, updatable = false, nullable = false)
    private UUID uuid;
    @Getter
    @Column(name = COLUMN_MAP_WIDTH, updatable = false)
    private int width;
    @Getter
    @Column(name = COLUMN_MAP_HEIGHT, updatable = false)
    private int height;
    @ElementCollection
    private Map<Coordinate, Tile> tiles;

    public ZombiesMap(int width, int height) {
        this.uuid = UUID.randomUUID();
        this.width = width;
        this.height = height;
        this.tiles = new HashMap<>();
    }

    public void set(int x, int y, Tile tile) {
        if(tile != Tile.WATER) {
            tiles.put(new Coordinate(x,y), tile);
        }
    }

    @Nullable
    public Tile get(int x, int y) {
        Tile ret = null;
        if( !(x < 0 || x >= width || y < 0 || y >= height) ) {
            ret = tiles.getOrDefault(new Coordinate(x, y), Tile.WATER);
        }
        return ret;
    }

    @NonNull
    public Tile[][] getAdjacent(int x, int y) {
        var ret = new Tile[3][3];
        for(int i = 0; i < 3; i++) {
            for(int j = 0; j < 3; j++) {
                ret[i][j] = get(x + (i-1), y + (j-1));
            }
        }
        return ret;
    }

    @NonNull
    public String dump() {
        var ret = new StringBuilder();
        for(int j = 0; j < height; j++) {
            for(int i = 0; i < width; i++) {
                var t = get(i, j);
                assert t != null;
                ret.append(t.tile);
            }
            ret.append('\n');
        }
        return ret.toString();
    }
}
