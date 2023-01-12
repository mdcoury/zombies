package ca.adaptor.zombies.game.repositories;

import ca.adaptor.zombies.game.model.ZombiesMapTile;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface ZombiesMapTileRepository  extends JpaRepository<ZombiesMapTile, UUID> {
    @Query("SELECT mt FROM #{#entityName} mt WHERE mt.id = :mapTileId")
    @NotNull Optional<ZombiesMapTile> findById(@NotNull UUID mapTileId);
}
