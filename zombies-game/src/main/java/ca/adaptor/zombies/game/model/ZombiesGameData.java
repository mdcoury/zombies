package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Getter
@Entity(name = TABLE_GAME_DATA)
@Table(name = TABLE_GAME_DATA)
public class ZombiesGameData {
    public static final int MAX_LIFE = 5;

    @Id
    @GeneratedValue
    @Column(name = COLUMN_GAME_DATA_ID, updatable = false, nullable = false)
    private UUID id;
    @Setter
    @Column(name = COLUMN_GAME_DATA_LOCATION, nullable = false)
    private ZombiesCoordinate location;
    @Column(name = COLUMN_GAME_DATA_CARD_IDS, nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    private List<UUID> eventCardIds;
    @Column(name = COLUMN_GAME_DATA_NUM_BULLETS, nullable = false)
    private int numBullets;
    @Column(name = COLUMN_GAME_DATA_NUM_LIFE, nullable = false)
    private int numLife;

    public boolean isPlayerDead() {
        return numLife <= 0;
    }

    public void incrementLife() { numLife = Math.min(++numLife, MAX_LIFE); }
    public void incrementBullets() { numBullets++;}
    public boolean decrementLife() {
        if(numLife > 0) {
            --numLife;
            return true;
        }
        return false;
    }
    public boolean decrementBullets() {
        if(numBullets > 0) {
            --numBullets;
            return true;
        }
        return false;
    }
}
