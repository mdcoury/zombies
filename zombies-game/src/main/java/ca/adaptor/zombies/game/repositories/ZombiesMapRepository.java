package ca.adaptor.zombies.game.repositories;

import ca.adaptor.zombies.game.model.ZombiesMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional
public interface ZombiesMapRepository extends JpaRepository<ZombiesMap, UUID> {

}