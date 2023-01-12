package ca.adaptor.zombies.game.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ZombiesEntityManagerHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ZombiesEntityManagerHelper.class);

    @PersistenceContext
    private EntityManager entityManager;

    public <T> T save(@NotNull T object) {
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
    public <T> Optional<T> findById(@NotNull UUID id, @NotNull Class<T> clazz) {
        return Optional.ofNullable(entityManager.find(clazz, id));
    }

    public boolean existsByName(@NotNull String name, @NotNull Class<?> clazz) {
        throw new RuntimeException();
    }

    @NotNull
    public <T> Optional<T> findByName(@NotNull String name, @NotNull Class<T> clazz) {
        throw new RuntimeException();
    }

    @NotNull
    public <T> List<T> findAll(@NotNull Class<T> clazz) {
        throw new RuntimeException();
    }
    @NotNull
    public <T> List<T> findAllById(@NotNull Collection<UUID> ids, @NotNull Class<T> clazz) {
        throw new RuntimeException();
    }

    @NotNull
    public List<UUID> findAllIds(@NotNull Class<?> clazz) {
        throw new RuntimeException();
    }

    @NotNull
    public <T> T update(@NotNull T object) {
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
