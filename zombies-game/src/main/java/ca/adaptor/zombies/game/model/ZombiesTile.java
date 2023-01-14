package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;
import static ca.adaptor.zombies.game.util.ZombiesQueryConstants.QUERY_FIND_TILE_BY_NAME;

@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
@Entity(name = TABLE_TILE)
@Table(
        name = TABLE_TILE,
        indexes = {
                @Index(name = INDEX_TILE_NAME, columnList = COLUMN_NAME, unique = true)
        }
)
@NamedQueries({
        @NamedQuery(
                name = QUERY_FIND_TILE_BY_NAME,
                query = "SELECT t FROM " + TABLE_TILE + " t WHERE t." + COLUMN_NAME + " = :name"
        )
})
public class ZombiesTile implements IZombieModelObject {
    public static final int NUM_SIDES = 4;
    public static final int TILE_SIZE = 3;

    public static final String TOWN_SQUARE = "Town Square";
    public static final String HELIPAD = "Helipad";

    public enum SquareType {
        IMPASSABLE,
        ROAD,
        BUILDING,
        DOOR,
        HELICOPTER,
        TOWN_SQUARE
        ;
    }

    @Id
    @GeneratedValue
    @Column(name = COLUMN_ID, updatable = false, nullable = false)
    private UUID id;
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated
    private SquareType[] squareTypes = new SquareType[TILE_SIZE*TILE_SIZE];
    @Column(name = COLUMN_NUM_ZOMBIES, updatable = false, nullable = false)
    private int numZombies;
    @Column(name = COLUMN_NUM_LIFE, updatable = false, nullable = false)
    private int numLife;
    @Column(name = COLUMN_NUM_BULLETS, updatable = false, nullable = false)
    private int numBullets;
    @Column(name = COLUMN_NAME, updatable = false, nullable = false, unique = true)
    private String name;

    public ZombiesTile(SquareType[] squareTypes, int numZombies, int numLife, int numBullets, String name) {
        assert squareTypes.length == TILE_SIZE*TILE_SIZE;
        System.arraycopy(squareTypes, 0, this.squareTypes, 0, TILE_SIZE*TILE_SIZE);
        this.numZombies = numZombies;
        this.numLife = numLife;
        this.numBullets = numBullets;
        this.name = name;
    }

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
                ret.add(new ZombiesCoordinate(i % TILE_SIZE, i / TILE_SIZE));
            }
        }
        return ret;
    }

    @NotNull
    public List<ZombiesDirection> getExits() {
        var ret = new ArrayList<ZombiesDirection>();
        if(squareTypes[1] == SquareType.ROAD) { ret.add(ZombiesDirection.NORTH); };
        if(squareTypes[3] == SquareType.ROAD) { ret.add(ZombiesDirection.WEST); };
        if(squareTypes[5] == SquareType.ROAD) { ret.add(ZombiesDirection.EAST); };
        if(squareTypes[7] == SquareType.ROAD) { ret.add(ZombiesDirection.SOUTH); };
        return ret;
    }
}