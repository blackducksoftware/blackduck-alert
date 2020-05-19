package com.synopsys.integration.alert.database.api.mock;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.database.certificates.CustomCertificateEntity;
import com.synopsys.integration.alert.database.certificates.CustomCertificateRepository;

public class MockCustomCertificateRepository extends DefaultMockJPARepository<CustomCertificateEntity, Long> implements CustomCertificateRepository {

    private Map<Long, CustomCertificateEntity> customCertificateEntityMapById = new HashMap<>();

    private long currentId = 0;

    public MockCustomCertificateRepository() {

    }

    public MockCustomCertificateRepository(String alias, String content, OffsetDateTime lastUpdated) {
        CustomCertificateEntity customCertificateEntity = new CustomCertificateEntity(alias, content, lastUpdated);
        this.save(customCertificateEntity);
    }

    @Override
    public Optional<CustomCertificateEntity> findByAlias(String alias) {
        return customCertificateEntityMapById.values()
                   .stream()
                   .filter(entity -> entity.getAlias().equals(alias))
                   .findFirst();
    }

    @Override
    public List<CustomCertificateEntity> findAll() {
        return new ArrayList<>(customCertificateEntityMapById.values());
    }

    @Override
    public Optional<CustomCertificateEntity> findById(Long id) {
        return Optional.ofNullable(customCertificateEntityMapById.get(id));
    }

    @Override
    public boolean existsById(Long id) {
        return customCertificateEntityMapById.containsKey(id);
    }

    @Override
    public <S extends CustomCertificateEntity> S save(S entity) {
        entity.setId(currentId);
        customCertificateEntityMapById.put(currentId, entity);
        currentId++;
        return entity;
    }

    @Override
    public void deleteById(Long id) {
        customCertificateEntityMapById.remove(id);
    }
}
