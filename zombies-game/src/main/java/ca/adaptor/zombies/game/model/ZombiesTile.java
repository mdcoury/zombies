package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_TILE_ID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
@Entity
@Table
public class ZombiesTile {
    public static final int NUM_SIDES = 4;
    public static final int TILE_SIZE = 3;
    public enum SquareType {
        IMPASSABLE,
        ROAD,
        BUILDING,
        DOOR,
        HELICOPTER,
        TOWN_SQUARE
        ;
    }
    public enum TileSide {
        NORTH, EAST, SOUTH, WEST
    }
    public enum TileRotation {
        ROT_0, ROT_90, ROT_180, ROT_270
    }

    public static final String TOWN_SQUARE = "Town Square";
    public static final String HELIPAD = "Helipad";

    @Id
    @Column(name = COLUMN_TILE_ID, updatable = false, nullable = false)
    private UUID id = UUID.randomUUID();
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated
    private SquareType[] squareTypes = new SquareType[9];
    @Column(updatable = false, nullable = false)
    private int numZombies = 0;
    @Column(updatable = false, nullable = false)
    private int numLife = 0;
    @Column(updatable = false, nullable = false)
    private int numBullets = 0;
    @Column(updatable = false, nullable = false)
    private String name;

    @NotNull
    public SquareType get(int x, int y) {
        return squareTypes[x + y*TILE_SIZE];
    }

    public boolean isBuilding() {
        return Arrays.stream(squareTypes)
                .anyMatch(
                        x -> x == SquareType.BUILDING
                                || x == SquareType.DOOR
                );
    }

    @NotNull
    public List<ZombiesCoordinate> getBuildingSquares() {
        var ret = new ArrayList<ZombiesCoordinate>();
        for(int i = 0; i < TILE_SIZE*TILE_SIZE; i++) {
            var type = squareTypes[i];
            if(type == SquareType.BUILDING || type == SquareType.DOOR) {
                ret.add(new ZombiesCoordinate(i%TILE_SIZE, i/TILE_SIZE));
            }
        }
        return ret;
    }

    @NotNull
    public List<TileSide> getExits() {
        var ret = new ArrayList<TileSide>();
        if(squareTypes[1] == SquareType.ROAD) { ret.add(TileSide.NORTH); };
        if(squareTypes[3] == SquareType.ROAD) { ret.add(TileSide.WEST); };
        if(squareTypes[5] == SquareType.ROAD) { ret.add(TileSide.EAST); };
        if(squareTypes[7] == SquareType.ROAD) { ret.add(TileSide.SOUTH); };
        return ret;
    }
}