package com.synopsys.integration.alert.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.repository.JpaRepository;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

// Line coverage test
public class RepositoryAccessorTest {
    private final JpaRepository<DatabaseEntity, Long> repository = Mockito.mock(JpaRepository.class);
    private RepositoryAccessor repositoryAccessor;

    @BeforeEach
    public void init() {
        repositoryAccessor = new RepositoryAccessor(repository) {
            @Override
            public DatabaseEntity saveEntity(final DatabaseEntity entity) {
                return repository.save(entity);
            }
        };
    }

    @Test
    public void readEntitiesTest() {
        repositoryAccessor.readEntities();
    }

    @Test
    public void readEntityTest() {
        repositoryAccessor.readEntity(1L);
    }

    @Test
    public void deleteEntityTest() {
        repositoryAccessor.deleteEntity(1L);
    }

    @Test
    public void deleteAllTest() {
        repositoryAccessor.deleteAll();
    }
}
