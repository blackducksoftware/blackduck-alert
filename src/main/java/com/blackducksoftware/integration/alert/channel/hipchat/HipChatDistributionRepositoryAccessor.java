package com.blackducksoftware.integration.alert.channel.hipchat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatDistributionRepository;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.RepositoryAccessor;

@Component
public class HipChatDistributionRepositoryAccessor extends RepositoryAccessor {
    private final HipChatDistributionRepository repository;

    @Autowired
    public HipChatDistributionRepositoryAccessor(final HipChatDistributionRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final HipChatDistributionConfigEntity hipChatEntity = (HipChatDistributionConfigEntity) entity;
        return repository.save(hipChatEntity);
    }

}
