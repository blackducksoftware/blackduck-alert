package com.blackducksoftware.integration.alert.channel.hipchat;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;

@Component
public class CommonDistributionDescriptor {
    private final CommonDistributionRepository commonDistributionRepository;

    @Autowired
    public CommonDistributionDescriptor(final CommonDistributionRepository commonDistributionRepository) {
        this.commonDistributionRepository = commonDistributionRepository;
    }

    public void saveDistribution(final ChannelDescriptor descriptor, final CommonDistributionConfigRestModel restModel) throws AlertException {
        final DatabaseEntity createdEntity = descriptor.convertFromDistributionRestModelToDistributionConfigEntity(restModel);
        final CommonDistributionConfigEntity commonEntity = convertFromModel(restModel);
        if (createdEntity != null && commonEntity != null) {
            final Optional<? extends DatabaseEntity> savedEntity = descriptor.saveDistributionEntity(createdEntity);
            if (savedEntity.isPresent()) {
                commonEntity.setDistributionConfigId(savedEntity.get().getId());
                commonDistributionRepository.save(commonEntity);
            }
        }
    }

    public CommonDistributionConfigEntity convertFromModel(final CommonDistributionConfigRestModel restModel) {
        final long commonId = Long.parseLong(restModel.getDistributionConfigId());
        final DigestTypeEnum digestType = DigestTypeEnum.valueOf(restModel.getFrequency());
        final boolean filterByProject = Boolean.parseBoolean(restModel.getFilterByProject());
        return new CommonDistributionConfigEntity(commonId, restModel.getDistributionType(), restModel.getName(), digestType, filterByProject);
    }
}
