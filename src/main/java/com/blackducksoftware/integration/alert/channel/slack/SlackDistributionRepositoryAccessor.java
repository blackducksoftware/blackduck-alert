package com.blackducksoftware.integration.alert.channel.slack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.slack.model.SlackDistributionRepository;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.RepositoryAccessor;

@Component
public class SlackDistributionRepositoryAccessor extends RepositoryAccessor {
    private final SlackDistributionRepository repository;

    @Autowired
    public SlackDistributionRepositoryAccessor(final SlackDistributionRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public DatabaseEntity saveEntity(final DatabaseEntity entity) {
        final SlackDistributionConfigEntity slackEntity = (SlackDistributionConfigEntity) entity;
        return repository.save(slackEntity);
    }

}
