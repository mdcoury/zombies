package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.*;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = TABLE_PLAYER)
@Table(name = TABLE_PLAYER)
public class ZombiesPlayer {
    @Id
    @GeneratedValue
    @Column(name = COLUMN_PLAYER_ID, updatable = false, nullable = false)
    private UUID id;
}
