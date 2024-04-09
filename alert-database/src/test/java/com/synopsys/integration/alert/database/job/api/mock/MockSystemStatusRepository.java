package com.synopsys.integration.alert.database.job.api.mock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;

import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.system.SystemStatusEntity;
import com.synopsys.integration.alert.database.system.SystemStatusRepository;

public class MockSystemStatusRepository extends DefaultMockJPARepository<SystemStatusEntity, Long> implements SystemStatusRepository {

    //Only methods that are used by a test are currently implemented, all others are left default.
    private SystemStatusEntity systemStatus;

    public MockSystemStatusRepository(Boolean startingStatus) {
        this.systemStatus = new SystemStatusEntity(startingStatus, DateUtils.createCurrentDateTimestamp());
    }

    @Override
    public List<SystemStatusEntity> findAll() {
        return List.of(systemStatus);
    }

    @Override
    public <S extends SystemStatusEntity> S save(S entity) {
        this.systemStatus = new SystemStatusEntity(entity.isInitialConfigurationPerformed(), entity.getStartupTime());

        return (S) this.systemStatus;
    }

    @Override
    public Optional<SystemStatusEntity> findById(Long aLong) {
        return Optional.ofNullable(this.systemStatus);
    }

    @Override
    public <S extends SystemStatusEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        List<S> savedItems = new LinkedList<>();
        Iterator<S> iterator = entities.iterator();
        while (iterator.hasNext()) {
            S entity = iterator.next();
            savedItems.add(save(entity));
        }
        return savedItems;
    }

    @Override
    public void deleteAllInBatch(Iterable<SystemStatusEntity> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        longs.forEach(this::deleteById);

    }

    @Override
    public SystemStatusEntity getById(Long aLong) {
        return findById(aLong).orElse(null);
    }

    @Override
    public SystemStatusEntity getReferenceById(Long aLong) {
        return getById(aLong);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        longs.forEach(this::deleteById);
    }

    @Override
    public <S extends SystemStatusEntity, R> R findBy(
        Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction
    ) {
        return null;
    }
}
