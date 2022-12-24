package ca.adaptor.zombies.game.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
@Embeddable
public class ZombiesCoordinate {
    private int x;
    private int y;
}
