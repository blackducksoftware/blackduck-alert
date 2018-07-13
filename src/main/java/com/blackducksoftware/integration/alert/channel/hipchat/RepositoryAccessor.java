package com.blackducksoftware.integration.alert.channel.hipchat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;

public abstract class RepositoryAccessor {
    private final JpaRepository<? extends DatabaseEntity, Long> repository;

    public RepositoryAccessor(final JpaRepository<? extends DatabaseEntity, Long> repository) {
        this.repository = repository;
    }

    public List<? extends DatabaseEntity> readDistributionEntities() {
        return repository.findAll();
    }

    public Optional<? extends DatabaseEntity> readDistributionEntity(final long id) {
        return repository.findById(id);
    }

    public void deleteDistributionEntity(final long id) {
        repository.deleteById(id);
    }

    public abstract DatabaseEntity saveDistributionEntity(final DatabaseEntity entity);
}
