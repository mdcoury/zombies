package ca.adaptor.zombies.game.repositories;

import ca.adaptor.zombies.game.model.ZombiesGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_GAME_GAME_DATA;

@Repository
public interface ZombiesGameRepository extends JpaRepository<ZombiesGame, UUID> {
    @Query("select g.id from #{#entityName} g")
    Optional<List<UUID>> findAllIds();
}
