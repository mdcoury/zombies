package ca.adaptor.zombies.game.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_EVENT_CARD_ID;
import static ca.adaptor.zombies.game.model.ZombiesModelConstants.TABLE_EVENT_CARD;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity(name = TABLE_EVENT_CARD)
@Table(name = TABLE_EVENT_CARD)
public class ZombiesEventCard implements IZombieModelObject {
    @Getter
    @Id
    @GeneratedValue
    @Column(name = COLUMN_EVENT_CARD_ID, updatable = false, nullable = false)
    private UUID id;

}
