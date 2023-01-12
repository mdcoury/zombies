package ca.adaptor.zombies.game.repositories;

import ca.adaptor.zombies.game.model.ZombiesTile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface ZombiesTileRepository  extends JpaRepository<ZombiesTile, UUID> {
    @Query("select g.id from #{#entityName} g")
    Optional<List<UUID>> findAllIds();
    @Query("select t.name from #{#entityName} t")
    Optional<List<String>> findAllNames();
    Optional<ZombiesTile> findByName(String name);
    boolean existsByName(String name);
}
