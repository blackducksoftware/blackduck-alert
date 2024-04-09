package com.synopsys.integration.alert.database.job.api.mock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.FluentQuery;

import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesEntity;
import com.synopsys.integration.alert.database.provider.task.ProviderTaskPropertiesRepository;

public class MockProviderTaskPropertiesRepository extends DefaultMockJPARepository<ProviderTaskPropertiesEntity, Long> implements ProviderTaskPropertiesRepository {

    private Map<Long, ProviderTaskPropertiesEntity> providerTaskPropertiesEntityMap = new HashMap<>();

    public MockProviderTaskPropertiesRepository(Long providerConfigId, String taskName, String propertyName, String value) {
        ProviderTaskPropertiesEntity providerTaskPropertiesEntity = new ProviderTaskPropertiesEntity(providerConfigId, taskName, propertyName, value);
        this.save(providerTaskPropertiesEntity);
    }

    @Override
    public Optional<ProviderTaskPropertiesEntity> findByTaskNameAndPropertyName(String taskName, String propertyName) {
        return providerTaskPropertiesEntityMap.values()
            .stream()
            .filter(entity -> entity.getTaskName().equals(taskName) && entity.getPropertyName().equals(propertyName))
            .findFirst();
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> S save(S entity) {
        providerTaskPropertiesEntityMap.put(entity.getProviderConfigId(), entity);
        return entity;
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        List<S> savedItems = new LinkedList<>();
        Iterator<S> iterator = entities.iterator();
        while (iterator.hasNext()) {
            S entity = iterator.next();
            savedItems.add(save(entity));
        }
        return savedItems;
    }

    @Override
    public void deleteAllInBatch(Iterable<ProviderTaskPropertiesEntity> entities) {
        entities.forEach(this::delete);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) {
        longs.forEach(this::deleteById);

    }

    @Override
    public ProviderTaskPropertiesEntity getById(Long aLong) {
        return providerTaskPropertiesEntityMap.get(aLong);
    }

    @Override
    public ProviderTaskPropertiesEntity getReferenceById(Long aLong) {
        return getById(aLong);
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        longs.forEach(this::deleteById);
    }

    @Override
    public <S extends ProviderTaskPropertiesEntity, R> R findBy(
        Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction
    ) {
        return null;
    }
}
