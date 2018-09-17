package com.synopsys.integration.alert.provider.blackduck.descriptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Set;

import javax.transaction.Transactional;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.common.workflow.processor.MessageContentCollector;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyMessageContentCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityMessageContentCollector;
import com.synopsys.integration.test.annotation.IntegrationTest;

@Category({ IntegrationTest.class })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class BlackDuckMessageContentCollectorFactoryTest {

    @Autowired
    private ObjectFactory<BlackDuckVulnerabilityMessageContentCollector> vulnerabilityTopicCollectorFactory;
    @Autowired
    private ObjectFactory<BlackDuckPolicyMessageContentCollector> policyTopicCollectorFactory;

    @Test
    public void testCollectorCreation() {
        final BlackDuckTopicCollectorFactory topicCollectorFactory = new BlackDuckTopicCollectorFactory(vulnerabilityTopicCollectorFactory, policyTopicCollectorFactory);
        final Set<MessageContentCollector> messageContentCollectorSet = topicCollectorFactory.createTopicCollectors();
        assertFalse(messageContentCollectorSet.isEmpty());
        assertEquals(2, messageContentCollectorSet.size());
        final Set<MessageContentCollector> differentReferenceMessageContentCollectorSet = topicCollectorFactory.createTopicCollectors();
        assertFalse(differentReferenceMessageContentCollectorSet.isEmpty());
        assertEquals(2, differentReferenceMessageContentCollectorSet.size());

        // make sure they are different object references since MessageContentCollector does implement equals or hashcode which is ok. we want different instances.
        assertFalse(messageContentCollectorSet.equals(differentReferenceMessageContentCollectorSet));
    }
}
