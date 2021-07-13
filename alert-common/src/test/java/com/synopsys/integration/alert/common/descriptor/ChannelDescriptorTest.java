package com.synopsys.integration.alert.common.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class ChannelDescriptorTest {

    @Test
    public void getterTest() {
        String name = "channel";
        ChannelKey channelKey = new ChannelKey(name, name) {};
        ChannelDescriptor channelDescriptor = new ChannelDescriptor(channelKey, null, null) {
            @Override
            public Optional<GlobalConfigurationValidator> getGlobalValidator() {
                return Optional.empty();
            }

            @Override
            public Optional<DistributionConfigurationValidator> getDistributionValidator() {
                return Optional.empty();
            }

            @Override
            public Set<DefinedFieldModel> getAllDefinedFields(ConfigContextEnum context) {
                return null;
            }
        };

        assertEquals(name, channelDescriptor.getDescriptorKey().getUniversalKey());
    }

}
