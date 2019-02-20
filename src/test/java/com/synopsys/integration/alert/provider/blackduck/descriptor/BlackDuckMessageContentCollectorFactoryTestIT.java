package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.descriptor.MessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckLicenseLimitCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyOverrideCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyViolationCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityCollector;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class BlackDuckMessageContentCollectorFactoryTestIT extends AlertIntegrationTest {

    @Autowired
    private ObjectFactory<BlackDuckVulnerabilityCollector> vulnerabilityTopicCollectorFactory;
    @Autowired
    private ObjectFactory<BlackDuckPolicyViolationCollector> policyViolationTopicCollectorFactory;
    @Autowired
    private ObjectFactory<BlackDuckPolicyOverrideCollector> policyOverrideTopicCollectorFactory;
    @Autowired
    private ObjectFactory<BlackDuckLicenseLimitCollector> licenseTopicCollectorFactory;

    @Test
    public void testCollectorCreation() {
        final BlackDuckTopicCollectorFactory topicCollectorFactory = new BlackDuckTopicCollectorFactory(vulnerabilityTopicCollectorFactory, policyViolationTopicCollectorFactory, policyOverrideTopicCollectorFactory,
            licenseTopicCollectorFactory);
        final Set<MessageContentCollector> messageContentCollectorSet = topicCollectorFactory.createTopicCollectors();
        assertFalse(messageContentCollectorSet.isEmpty());
        assertEquals(4, messageContentCollectorSet.size());
        final Set<MessageContentCollector> differentReferenceMessageContentCollectorSet = topicCollectorFactory.createTopicCollectors();
        assertFalse(differentReferenceMessageContentCollectorSet.isEmpty());
        assertEquals(4, differentReferenceMessageContentCollectorSet.size());

        // make sure they are different object references since MessageContentCollector does implement equals or hashcode which is ok. we want different instances.
        assertFalse(messageContentCollectorSet.equals(differentReferenceMessageContentCollectorSet));
    }
}
