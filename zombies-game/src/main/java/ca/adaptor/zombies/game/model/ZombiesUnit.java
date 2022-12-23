package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Entity
@Table
public class ZombiesUnit {
    enum Type {
        INFANTRY,
        CAVALRY,
        ;
    }

    @Id
    @Column(name = ZombiesModelConstants.COLUMN_UNIT_ID, updatable = false, nullable = false)
    private UUID uuid = UUID.randomUUID();
    @Enumerated
    private Type type;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private ZombiesMap map;
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private ZombiesPlayer owner;
    @Embedded
    @Column
    private ZombiesMap.Coordinate location;
}
