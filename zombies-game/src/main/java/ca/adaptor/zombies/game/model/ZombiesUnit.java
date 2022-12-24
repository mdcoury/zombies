package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_UNIT_ID;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table
public class ZombiesUnit {
    enum Type {
        ZOMBIE
        ;
    }

    @Id
    @Column(name = COLUMN_UNIT_ID, updatable = false, nullable = false)
    private UUID uuid = UUID.randomUUID();
    @Enumerated
    private Type type;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private ZombiesMap map;
    @Embedded
    @Column
    private ZombiesCoordinate location;
}
