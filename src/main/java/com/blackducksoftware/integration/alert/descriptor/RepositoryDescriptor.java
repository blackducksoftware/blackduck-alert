package com.blackducksoftware.integration.alert.descriptor;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;

public class RepositoryDescriptor {
    private final JpaRepository<DatabaseEntity, Long> repository;

    public RepositoryDescriptor(final JpaRepository<DatabaseEntity, Long> repository) {
        this.repository = repository;
    }

    public List<? extends DatabaseEntity> readDistributionEntities() {
        return repository.findAll();
    }

    public Optional<? extends DatabaseEntity> readDistributionEntity(final long id) {
        return repository.findById(id);
    }

    public Optional<? extends DatabaseEntity> saveDistributionEntity(final DatabaseEntity entity) {
        return Optional.of(repository.save(entity));
    }

    public void deleteDistributionEntity(final long id) {
        repository.deleteById(id);
    }
}
