package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_ID;
import static ca.adaptor.zombies.game.model.ZombiesModelConstants.TABLE_PLAYER;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = TABLE_PLAYER)
@Table(name = TABLE_PLAYER)
public class ZombiesPlayer implements IZombieModelObject {
    @Id
    @GeneratedValue
    @Column(name = COLUMN_ID, updatable = false, nullable = false)
    private UUID id;

}
