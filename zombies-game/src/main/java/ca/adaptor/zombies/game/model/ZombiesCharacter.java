package ca.adaptor.zombies.game.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_CHARACTER_ID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table
public class ZombiesCharacter {

    @Getter
    @Id
    @Column(name = COLUMN_CHARACTER_ID, updatable = false, nullable = false)
    private UUID id;
}
