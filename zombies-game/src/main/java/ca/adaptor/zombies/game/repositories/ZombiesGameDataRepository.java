package ca.adaptor.zombies.game.repositories;

import ca.adaptor.zombies.game.model.ZombiesGameData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ZombiesGameDataRepository extends JpaRepository<ZombiesGameData, UUID> {

}
