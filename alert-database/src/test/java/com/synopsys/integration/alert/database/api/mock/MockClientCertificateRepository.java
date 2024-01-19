package com.synopsys.integration.alert.database.api.mock;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.synopsys.integration.alert.database.certificates.ClientCertificateEntity;
import com.synopsys.integration.alert.database.certificates.ClientCertificateRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockClientCertificateRepository extends MockRepositoryContainer<UUID, ClientCertificateEntity> implements ClientCertificateRepository {
    public MockClientCertificateRepository() {
        super(ClientCertificateEntity::getId);
    }

    @Override
    public @NotNull ClientCertificateEntity save(@NotNull ClientCertificateEntity entity) {
        // For the case where id is null; the db generates an id for us.
        // Since this mock data map doesn't do so, we will generate the id here.
        UUID id = getIdGenerator().apply(entity);
        if (id == null) {
            id = UUID.randomUUID();
            entity.setId(id);
        }
        getDataMap().put(id, entity);
        return getDataMap().get(id);
    }

    @Override
    public Optional<ClientCertificateEntity> findByAlias(String alias) {
        return findAll()
                .stream()
                .filter(entity -> entity.getAlias().equals(alias))
                .findFirst();
    }
}
