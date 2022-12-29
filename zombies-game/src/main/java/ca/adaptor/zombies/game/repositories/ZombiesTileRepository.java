package ca.adaptor.zombies.game.repositories;

import ca.adaptor.zombies.game.model.ZombiesTile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ZombiesTileRepository  extends JpaRepository<ZombiesTile, UUID> {
    ZombiesTile findByName(String name);
    boolean existsByName(String name);
}
