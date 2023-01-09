package ca.adaptor.zombies.game.repositories;

import ca.adaptor.zombies.game.model.ZombiesGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface ZombiesGameRepository extends JpaRepository<ZombiesGame, UUID> {
    @Query("SELECT g.id FROM #{#entityName} g")
    Optional<List<UUID>> findAllIds();
}
