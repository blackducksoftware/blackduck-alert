package com.synopsys.integration.alert.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.jpa.repository.JpaRepository;

import com.synopsys.integration.alert.database.entity.DatabaseEntity;

// Line coverage populateFieldModel
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
        Mockito.verify(repository).findAll();
    }

    @Test
    public void readEntityTest() {
        final Long id = 1L;
        repositoryAccessor.readEntity(id);
        Mockito.verify(repository).findById(id);
    }

    @Test
    public void deleteEntityTest() {
        final Long id = 1L;
        repositoryAccessor.deleteEntity(id);
        Mockito.verify(repository).deleteById(id);
    }

    @Test
    public void deleteAllTest() {
        repositoryAccessor.deleteAll();
        Mockito.verify(repository).deleteAllInBatch();
    }
}
