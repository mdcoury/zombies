package ca.adaptor.zombies.game.repositories;

import ca.adaptor.zombies.game.model.ZombiesMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ZombiesMapRepository extends JpaRepository<ZombiesMap, UUID> {
    @Query("select m.id from #{#entityName} m")
    Optional<List<UUID>> findAllIds();
}