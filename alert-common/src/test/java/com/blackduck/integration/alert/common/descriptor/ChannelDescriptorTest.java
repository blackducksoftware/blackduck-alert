package com.blackduck.integration.alert.common.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import com.blackduck.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.blackduck.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;

public class ChannelDescriptorTest {
    @Test
    public void getterTest() {
        String name = "channel";
        ChannelKey channelKey = new ChannelKey(name, name) {};
        ChannelDescriptor channelDescriptor = new ChannelDescriptor(channelKey, Set.of(ConfigContextEnum.DISTRIBUTION)) {
            @Override
            public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
                return Optional.empty();
            }

            @Override
            public Optional<DistributionConfigurationValidator> getDistributionValidator() {
                return Optional.empty();
            }

        };

        assertEquals(name, channelDescriptor.getDescriptorKey().getUniversalKey());
    }

}
