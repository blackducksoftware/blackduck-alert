package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.database.api.ProviderProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;

public class MockBlackDuckProjectRepositoryAccessor extends ProviderProjectRepositoryAccessor {
    private final Map<Long, ProviderProjectEntity> blackDuckProjectEntityMap = new HashMap<>();
    private Long count = 1L;

    public MockBlackDuckProjectRepositoryAccessor() {
        super(null);
    }

    @Override
    public ProviderProjectEntity saveEntity(final ProviderProjectEntity providerProjectEntity) {
        final ProviderProjectEntity newEntity = new ProviderProjectEntity(providerProjectEntity.getName(), providerProjectEntity.getDescription(), providerProjectEntity.getHref(), providerProjectEntity.getProjectOwnerEmail(),
            providerProjectEntity.getProvider());
        if (null == providerProjectEntity.getId()) {
            newEntity.setId(count);
            count++;
        } else {
            newEntity.setId(providerProjectEntity.getId());
        }
        blackDuckProjectEntityMap.put(newEntity.getId(), newEntity);
        return newEntity;
    }

    @Override
    public List<ProviderProjectEntity> readEntities() {
        return new ArrayList<>(blackDuckProjectEntityMap.values());
    }

    @Override
    public List<ProviderProjectEntity> findByProviderName(final String providerName) {
        return blackDuckProjectEntityMap.values()
                   .stream()
                   .filter(entity -> providerName.equals(entity.getProvider()))
                   .collect(Collectors.toList());
    }

    @Override
    public Optional<ProviderProjectEntity> readEntity(final long id) {
        return Optional.ofNullable(blackDuckProjectEntityMap.get(Long.valueOf(id)));
    }

    @Override
    public void deleteEntity(final long id) {
        blackDuckProjectEntityMap.remove(Long.valueOf(id));
    }

    @Override
    public List<ProviderProjectEntity> deleteAndSaveAll(final Iterable<ProviderProjectEntity> blackDuckProjectEntities) {
        blackDuckProjectEntityMap.clear();
        final List<ProviderProjectEntity> blackDuckProjectEntitiesSaved = new ArrayList<>();
        blackDuckProjectEntities.forEach(blackDuckProjectEntity -> {
            blackDuckProjectEntitiesSaved.add(saveEntity(blackDuckProjectEntity));
        });
        return blackDuckProjectEntitiesSaved;
    }

    @Override
    public ProviderProjectEntity findByName(final String name) {
        final Optional<ProviderProjectEntity> optionalBlackDuckProjectEntity = blackDuckProjectEntityMap.entrySet()
                                                                                   .stream()
                                                                                   .map(entry -> entry.getValue())
                                                                                   .filter(blackDuckProjectEntity -> name.equals(blackDuckProjectEntity.getName()))
                                                                                   .findFirst();
        return optionalBlackDuckProjectEntity.orElse(null);
    }

}
