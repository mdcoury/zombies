package ca.adaptor.zombies.game.repositories;

import ca.adaptor.zombies.game.model.ZombiesGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ZombiesGameRepository extends JpaRepository<ZombiesGame, UUID> {
    @Query("select g.id from #{#entityName} g")
    List<UUID> findAllIds();
}
