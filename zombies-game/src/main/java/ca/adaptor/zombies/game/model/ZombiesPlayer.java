package ca.adaptor.zombies.game.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_MAP_ID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table
public class ZombiesPlayer {
    @Id
    @Column(name = COLUMN_MAP_ID, updatable = false, nullable = false)
    private UUID uuid;
}
