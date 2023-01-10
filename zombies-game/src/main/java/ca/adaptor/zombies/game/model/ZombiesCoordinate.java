package ca.adaptor.zombies.game.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Embeddable;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
@Embeddable
public class ZombiesCoordinate {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private int x;
    private int y;
    @Override
    public String toString() {
        try {
            return MAPPER.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
