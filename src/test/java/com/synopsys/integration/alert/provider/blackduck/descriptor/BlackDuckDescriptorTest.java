package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.model.DefinedFieldModel;
import com.synopsys.integration.alert.common.workflow.MessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityCollector;

public class BlackDuckDescriptorTest {
    @Test
    public void testGetDefinedFields() {
        final BlackDuckTopicCollectorFactory topicCollectorFactory = Mockito.mock(BlackDuckTopicCollectorFactory.class);
        final List<MessageContentCollector> collectorList = Arrays.asList(Mockito.mock(BlackDuckVulnerabilityCollector.class), Mockito.mock(BlackDuckPolicyCollector.class));
        final Set<MessageContentCollector> expectedCollectorSet = new HashSet<>(collectorList);
        Mockito.when(topicCollectorFactory.createTopicCollectors()).thenReturn(expectedCollectorSet);

        final BlackDuckContent blackDuckContent = new BlackDuckContent();
        final BlackDuckDistributionUIConfig blackDuckDistributionUIConfig = new BlackDuckDistributionUIConfig(blackDuckContent);
        final BlackDuckProviderUIConfig blackDuckProviderUIConfig = new BlackDuckProviderUIConfig();
        final BlackDuckDescriptor descriptor = new BlackDuckDescriptor(blackDuckProviderUIConfig, blackDuckDistributionUIConfig);
        Set<DefinedFieldModel> fields = descriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL);
        assertEquals(3, fields.size());

        fields = descriptor.getAllDefinedFields(ConfigContextEnum.DISTRIBUTION);
        assertEquals(5, fields.size());
    }

}
