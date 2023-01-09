package ca.adaptor.zombies.game.repositories;

import ca.adaptor.zombies.game.model.ZombiesPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
@Transactional
public interface ZombiesPlayerRepository extends JpaRepository<ZombiesPlayer, UUID> {
}
