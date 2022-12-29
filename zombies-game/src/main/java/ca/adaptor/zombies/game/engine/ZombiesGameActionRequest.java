package ca.adaptor.zombies.game.engine;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ZombiesGameActionRequest {
    public enum Type {
        ROLL_REQUEST,
        USE_BULLETS_REQUEST,
        MOVEMENT_REQUEST,
        DISCARD_REQUEST
    }

    private Type type;
}
