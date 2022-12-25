package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;
import static ca.adaptor.zombies.game.model.ZombiesTile.TILE_SIZE;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table
public class ZombiesMapTile {
    @Getter
    @Id
    @Column(name = COLUMN_MAP_TILE_ID, updatable = false, nullable = false)
    private UUID id;
    @Getter
    @JoinColumn(name = COLUMN_TILE_ID, nullable = false, updatable = false)
    @ManyToOne(fetch = FetchType.EAGER)
    private ZombiesTile tile;
    @Getter
    @Enumerated
    private ZombiesTile.TileRotation rotation;
    @Getter
    @Embedded
    private ZombiesCoordinate topLeft;

    @Transient
    private final ZombiesTile.SquareType[] squaresCache = new ZombiesTile.SquareType[TILE_SIZE*TILE_SIZE];
    @Transient
    private boolean cached = false;

    public ZombiesMapTile(@NotNull ZombiesTile tile, @NotNull ZombiesCoordinate topLeft, ZombiesTile.TileRotation rotation) {
        this.id = UUID.randomUUID();
        this.tile = tile;
        this.topLeft = topLeft;
        this.rotation = rotation;
    }

    private void cacheRotation() {
        var squares = tile.getSquareTypes();
        switch(rotation) {
            case ROT_0 -> {
                System.arraycopy(squares, 0, squaresCache, 0, 9);
            }
            case ROT_90 -> {
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
            case ROT_180 -> {
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
            case ROT_270 -> {
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
    }

    @NotNull
    public ZombiesTile.SquareType get(int x, int y) {
        if(!cached) {
            cacheRotation();
            cached = true;
        }
        return squaresCache[x + y*TILE_SIZE];
    }
}