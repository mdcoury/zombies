package ca.adaptor.zombies.game.util;

import ca.adaptor.zombies.game.model.IZombieModelObject;
import ca.adaptor.zombies.game.model.ZombiesTile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Table;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_ID;
import static ca.adaptor.zombies.game.model.ZombiesModelConstants.COLUMN_TILE_NAME;

@Component
public class ZombiesEntityManagerHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesEntityManagerHelper.class);

    @PersistenceContext
    private EntityManager entityManager;

    public <T extends IZombieModelObject> T save(@NotNull T object) {
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(object);
            entityManager.getTransaction().commit();
            LOGGER.trace("Persisted object: " + object);
            return object;
        }
        catch(Exception e) {
            entityManager.getTransaction().rollback();
            LOGGER.warn("An exception occurred while persisting object: " + object, e);
            throw e;
        }
    }

    @NotNull
    public <T extends IZombieModelObject> Optional<T> findById(@NotNull UUID id, @NotNull Class<T> clazz) {
        return Optional.ofNullable(entityManager.find(clazz, id));
    }

    public <T extends IZombieModelObject>  boolean existsByName(@NotNull String name) {
        return findByName(name).isPresent();
    }

    @NotNull
    public Optional<ZombiesTile> findByName(@NotNull String name) {
        var tableName = ZombiesTile.class.getAnnotation(Table.class).name();
        var query = entityManager.createQuery(
                "SELECT t FROM " + tableName + " t WHERE t." + COLUMN_TILE_NAME + " = '" + name + "'",
                ZombiesTile.class
        );
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
                "SELECT t FROM " + tableName + " t WHERE t." + (tableName + COLUMN_ID) + " IN :ids",
                clazz
        );
        query.setParameter("ids", ids);
        return query.getResultList();
    }

    @NotNull
    public <T extends IZombieModelObject> List<UUID> findAllIds(@NotNull Class<T> clazz) {
        var tableName = clazz.getAnnotation(Table.class).name();
        var query = entityManager.createQuery(
                "SELECT t." + (tableName + COLUMN_ID) + " FROM " + tableName + " t",
                UUID.class
        );
        return query.getResultList();
    }

    @NotNull
    public <T extends IZombieModelObject> T update(@NotNull T object) {
        try {
            entityManager.getTransaction().begin();
            var merged = entityManager.merge(object);
            entityManager.getTransaction().commit();
            LOGGER.trace("Updated object: " + merged);
            return merged;
        }
        catch(Exception e) {
            entityManager.getTransaction().rollback();
            LOGGER.warn("An exception occurred while persisting object: " + object, e);
            throw e;
        }
    }
}
