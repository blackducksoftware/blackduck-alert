package com.blackducksoftware.integration.alert.descriptor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;

public abstract class RepositoryAccessor {
    private final JpaRepository<? extends DatabaseEntity, Long> repository;

    public RepositoryAccessor(final JpaRepository<? extends DatabaseEntity, Long> repository) {
        this.repository = repository;
    }

    public List<? extends DatabaseEntity> readEntities() {
        return repository.findAll();
    }

    public Optional<? extends DatabaseEntity> readEntity(final long id) {
        return repository.findById(id);
    }

    public void deleteEntity(final long id) {
        repository.deleteById(id);
    }

    public abstract DatabaseEntity saveEntity(final DatabaseEntity entity);
}
