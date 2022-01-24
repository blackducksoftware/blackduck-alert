package com.synopsys.integration.alert.test.common;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.descriptor.api.AzureBoardsChannelKey;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.descriptor.api.JiraCloudChannelKey;
import com.synopsys.integration.alert.descriptor.api.JiraServerChannelKey;
import com.synopsys.integration.alert.descriptor.api.MsTeamsKey;
import com.synopsys.integration.alert.descriptor.api.SlackChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public class MockDescriptorMap extends DescriptorMap {
    static List<DescriptorKey> descriptorKeys;
    static List<Descriptor> descriptors;

    static {
        descriptorKeys = List.of(
            new SlackChannelKey(),
            new MsTeamsKey(),
            new EmailChannelKey(),
            new BlackDuckProviderKey(),
            new AzureBoardsChannelKey(),
            new JiraCloudChannelKey(),
            new JiraServerChannelKey()
        );
        descriptors = List.of(
            new Descriptor(new SlackChannelKey(), DescriptorType.CHANNEL, Set.of(ConfigContextEnum.DISTRIBUTION)) {
                @Override
                public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
                    return Optional.empty();
                }

                @Override
                public Optional<DistributionConfigurationValidator> getDistributionValidator() {
                    return Optional.empty();
                }
            },
            new Descriptor(new EmailChannelKey(), DescriptorType.CHANNEL, Set.of(ConfigContextEnum.DISTRIBUTION, ConfigContextEnum.GLOBAL)) {
                @Override
                public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
                    return Optional.empty();
                }

                @Override
                public Optional<DistributionConfigurationValidator> getDistributionValidator() {
                    return Optional.empty();
                }
            },
            new Descriptor(new JiraCloudChannelKey(), DescriptorType.CHANNEL, Set.of(ConfigContextEnum.DISTRIBUTION, ConfigContextEnum.GLOBAL)) {
                @Override
                public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
                    return Optional.empty();
                }

                @Override
                public Optional<DistributionConfigurationValidator> getDistributionValidator() {
                    return Optional.empty();
                }
            },
            new Descriptor(new BlackDuckProviderKey(), DescriptorType.PROVIDER, Set.of(ConfigContextEnum.DISTRIBUTION, ConfigContextEnum.GLOBAL)) {
                @Override
                public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
                    return Optional.empty();
                }

                @Override
                public Optional<DistributionConfigurationValidator> getDistributionValidator() {
                    return Optional.empty();
                }
            }
        );
    }

    public MockDescriptorMap() {
        super(descriptorKeys, descriptors);
    }
}
