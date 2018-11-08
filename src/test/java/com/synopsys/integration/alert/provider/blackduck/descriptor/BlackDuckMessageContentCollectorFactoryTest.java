package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Set;

import org.junit.Test;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckLicenseLimitMessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyMessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityMessageContentCollector;

public class BlackDuckMessageContentCollectorFactoryTest extends AlertIntegrationTest {

    @Autowired
    private ObjectFactory<BlackDuckVulnerabilityMessageContentCollector> vulnerabilityTopicCollectorFactory;
    @Autowired
    private ObjectFactory<BlackDuckPolicyMessageContentCollector> policyTopicCollectorFactory;
    @Autowired
    private ObjectFactory<BlackDuckLicenseLimitMessageContentCollector> licenseTopicCollectorFactory;

    @Test
    public void testCollectorCreation() {
        final BlackDuckTopicCollectorFactory topicCollectorFactory = new BlackDuckTopicCollectorFactory(vulnerabilityTopicCollectorFactory, policyTopicCollectorFactory, licenseTopicCollectorFactory);
        final Set<MessageContentCollector> messageContentCollectorSet = topicCollectorFactory.createTopicCollectors();
        assertFalse(messageContentCollectorSet.isEmpty());
        assertEquals(3, messageContentCollectorSet.size());
        final Set<MessageContentCollector> differentReferenceMessageContentCollectorSet = topicCollectorFactory.createTopicCollectors();
        assertFalse(differentReferenceMessageContentCollectorSet.isEmpty());
        assertEquals(3, differentReferenceMessageContentCollectorSet.size());

        // make sure they are different object references since MessageContentCollector does implement equals or hashcode which is ok. we want different instances.
        assertFalse(messageContentCollectorSet.equals(differentReferenceMessageContentCollectorSet));
    }
}
