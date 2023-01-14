package ca.adaptor.zombies.game.util;

import ca.adaptor.zombies.game.model.IZombieModelObject;
import ca.adaptor.zombies.game.model.ZombiesTile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_ID;
import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_NAME;
import static ca.adaptor.zombies.game.util.ZombiesQueryConstants.QUERY_FIND_TILE_BY_NAME;

@Component
public class ZombiesEntityManagerHelper {
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    @NotNull
    public <T extends IZombieModelObject> T save(@NotNull T object) {
        entityManager.persist(object);
        return object;
    }

    @NotNull
    public <T extends IZombieModelObject> Optional<T> findById(@NotNull UUID id, @NotNull Class<T> clazz) {
        return Optional.ofNullable(entityManager.find(clazz, id));
    }

    @NotNull
    public Optional<ZombiesTile> findTileByName(@NotNull String name) {
        var query = entityManager.createNamedQuery(QUERY_FIND_TILE_BY_NAME, ZombiesTile.class);
        query.setParameter(COLUMN_NAME, name);
        var result = query.getResultList();
        return switch (result.size()) {
            case 0 -> Optional.empty();
            case 1 -> Optional.of(result.get(0));
            default -> throw new IllegalStateException("The name column should be unique!");
        };
    }

    @NotNull
    public <T extends IZombieModelObject> List<T> findAll(@NotNull Class<T> clazz) {
        var tableName = clazz.getAnnotation(Table.class).name();
        var query = entityManager.createQuery("SELECT t FROM " + tableName + " t", clazz);
        return query.getResultList();
    }
    @NotNull
    public <T extends IZombieModelObject> List<T> findAllById(@NotNull Collection<UUID> ids, @NotNull Class<T> clazz) {
        var tableName = clazz.getAnnotation(Table.class).name();
        var query = entityManager.createQuery(
                "SELECT t FROM " + tableName + " t WHERE t." + COLUMN_ID + " IN :ids",
                clazz
        );
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @NotNull
    public <T extends IZombieModelObject> List<UUID> findAllIds(@NotNull Class<T> clazz) {
        var tableName = clazz.getAnnotation(Table.class).name();
        var query = entityManager.createQuery(
                "SELECT t." + COLUMN_ID + " FROM " + tableName + " t",
                UUID.class
        );
        return query.getResultList();
    }

    @Transactional
    @NotNull
    public <T extends IZombieModelObject> T update(@NotNull T object) {
        return entityManager.merge(object);
    }
}
