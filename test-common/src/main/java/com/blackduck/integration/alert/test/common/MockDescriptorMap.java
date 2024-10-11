/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.test.common;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.blackduck.integration.alert.api.descriptor.AzureBoardsChannelKey;
import com.blackduck.integration.alert.api.descriptor.BlackDuckProviderKey;
import com.blackduck.integration.alert.api.descriptor.EmailChannelKey;
import com.blackduck.integration.alert.api.descriptor.JiraCloudChannelKey;
import com.blackduck.integration.alert.api.descriptor.JiraServerChannelKey;
import com.blackduck.integration.alert.api.descriptor.MsTeamsKey;
import com.blackduck.integration.alert.api.descriptor.SlackChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.common.descriptor.Descriptor;
import com.blackduck.integration.alert.common.descriptor.DescriptorMap;
import com.blackduck.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;

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
