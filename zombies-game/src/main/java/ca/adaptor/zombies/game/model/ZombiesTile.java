package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
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
    public enum SquareType {
        IMPASSABLE,
        ROAD,
        BUILDING,
        DOOR,
        HELICOPTER,
        TOWN_SQUARE;
    }
    public enum TileSide {
        NORTH, SOUTH, EAST, WEST
    }

    public static final String TOWN_SQUARE = "Town Square";
    public static final String HELIPAD = "Helipad";

    @Id
    @Column(name = COLUMN_TILE_ID, updatable = false, nullable = false)
    private UUID uuid = UUID.randomUUID();
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated
    private SquareType[] squareTypes = new SquareType[9];
    @Column(updatable = false, nullable = false)
    private int startingZombies = 0;
    @Column(updatable = false, nullable = false)
    private int startingLife = 0;
    @Column(updatable = false, nullable = false)
    private int startingBullets = 0;
    @Column(updatable = false)
    private String name;

    public SquareType get(int x, int y) {
        return squareTypes[x + y*3];
    }

    public List<TileSide> getExits() {
        var ret = new ArrayList<TileSide>();
        if(squareTypes[1] == SquareType.ROAD) { ret.add(TileSide.NORTH); };
        if(squareTypes[3] == SquareType.ROAD) { ret.add(TileSide.WEST); };
        if(squareTypes[5] == SquareType.ROAD) { ret.add(TileSide.EAST); };
        if(squareTypes[7] == SquareType.ROAD) { ret.add(TileSide.SOUTH); };
        return ret;
    }
}