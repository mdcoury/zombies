package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cascade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;
import static ca.adaptor.zombies.game.model.ZombiesTile.TILE_SIZE;
import static org.hibernate.annotations.CascadeType.MERGE;
import static org.hibernate.annotations.CascadeType.SAVE_UPDATE;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = TABLE_MAP_TILE)
@Table(name = TABLE_MAP_TILE)
public class ZombiesMapTile implements IZombieModelObject {
    public enum TileRotation {
        ROT_0, ROT_90, ROT_180, ROT_270
    }

    @Getter
    @Id
    @GeneratedValue
    @Column(name = COLUMN_ID, updatable = false, nullable = false)
    private UUID id;
    @Cascade(value = { MERGE, SAVE_UPDATE })
    @JoinColumn(name = COLUMN_TILE_ID, nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private ZombiesTile tile;
    @Getter
    @Column(name = COLUMN_ROTATION)
    @Enumerated
    private TileRotation rotation;
    @Getter @Setter
    @Column(name = COLUMN_TOP_LEFT)
    @Embedded
    private ZombiesCoordinate topLeft;

    @Transient
    private final ZombiesTile.SquareType[] squaresCache = new ZombiesTile.SquareType[TILE_SIZE*TILE_SIZE];
    @Transient
    private boolean cached = false;

    public ZombiesMapTile(@NotNull ZombiesTile tile, @NotNull ZombiesCoordinate topLeft, @NotNull TileRotation rotation) {
        this.tile = tile;
        this.topLeft = topLeft;
        this.rotation = rotation;
    }

    private void tryCacheRotation() {
        if(cached) {
            return;
        }

        var squares = tile.getSquareTypes();
        switch(rotation) {
            case ROT_0 ->
            {
                System.arraycopy(squares, 0, squaresCache, 0, 9);
            }
            case ROT_90 ->
            {
                squaresCache[0] = squares[6];
                squaresCache[1] = squares[3];
                squaresCache[2] = squares[0];
                squaresCache[3] = squares[7];
                squaresCache[4] = squares[4];
                squaresCache[5] = squares[1];
                squaresCache[6] = squares[8];
                squaresCache[7] = squares[5];
                squaresCache[8] = squares[2];
            }
            case ROT_180 ->
            {
                squaresCache[0] = squares[8];
                squaresCache[1] = squares[7];
                squaresCache[2] = squares[6];
                squaresCache[3] = squares[5];
                squaresCache[4] = squares[4];
                squaresCache[5] = squares[3];
                squaresCache[6] = squares[2];
                squaresCache[7] = squares[1];
                squaresCache[8] = squares[0];
            }
            case ROT_270 ->
            {
                squaresCache[0] = squares[2];
                squaresCache[1] = squares[5];
                squaresCache[2] = squares[8];
                squaresCache[3] = squares[1];
                squaresCache[4] = squares[4];
                squaresCache[5] = squares[7];
                squaresCache[6] = squares[0];
                squaresCache[7] = squares[3];
                squaresCache[8] = squares[6];
            }
            default -> throw new IllegalArgumentException();
        }
        cached = true;
    }

    @NotNull
    public ZombiesTile.SquareType[] getSquareTypes() {
        tryCacheRotation();
        return Arrays.copyOf(squaresCache, squaresCache.length);
    }

    @NotNull
    public UUID getTileId() { return tile.getId(); }
    public boolean isTownSquare() { return tile.getName().equals(ZombiesTile.TOWN_SQUARE); }
    public boolean isHelipad() { return tile.getName().equals(ZombiesTile.HELIPAD); }
    public boolean isBuilding() { return tile.isBuilding(); }
    public int getNumBullets() { return tile.getNumBullets(); }
    public int getNumLife() { return tile.getNumLife(); }
    public int getNumZombies() { return tile.getNumZombies(); }
    @NotNull
    public List<ZombiesDirection> getExits() {
        tryCacheRotation();
        var ret = new ArrayList<ZombiesDirection>();
        if(squaresCache[1] == ZombiesTile.SquareType.ROAD) { ret.add(ZombiesDirection.NORTH); };
        if(squaresCache[3] == ZombiesTile.SquareType.ROAD) { ret.add(ZombiesDirection.WEST); };
        if(squaresCache[5] == ZombiesTile.SquareType.ROAD) { ret.add(ZombiesDirection.EAST); };
        if(squaresCache[7] == ZombiesTile.SquareType.ROAD) { ret.add(ZombiesDirection.SOUTH); };
        return ret;
    }
    @NotNull
    public List<ZombiesCoordinate> getBuildingSquares() {
        tryCacheRotation();
        var ret = new ArrayList<ZombiesCoordinate>();
        for(int i = 0; i < TILE_SIZE*TILE_SIZE; i++) {
            var type = squaresCache[i];
            if(type == ZombiesTile.SquareType.BUILDING || type == ZombiesTile.SquareType.DOOR) {
                ret.add(new ZombiesCoordinate(i % TILE_SIZE, i / TILE_SIZE));
            }
        }
        return ret;
    }

    @NotNull
    public ZombiesTile.SquareType get(int x, int y) {
        tryCacheRotation();
        return squaresCache[x + y*TILE_SIZE];
    }

    @Nullable
    public ZombiesTile.SquareType getSquareType(int x, int y) {
        return getSquareType(new ZombiesCoordinate(x, y));
    }

    @Nullable
    public ZombiesTile.SquareType getSquareType(@NotNull ZombiesCoordinate xy) {
        return get(xy.getX() % TILE_SIZE, xy.getY() % TILE_SIZE);
    }
}