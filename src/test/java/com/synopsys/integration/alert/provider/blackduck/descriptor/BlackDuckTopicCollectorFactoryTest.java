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
import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckPolicyTopicCollector;
import com.synopsys.integration.alert.provider.blackduck.collector.BlackDuckVulnerabilityTopicCollector;
import com.synopsys.integration.test.annotation.IntegrationTest;

@Category({ IntegrationTest.class })
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public class BlackDuckTopicCollectorFactoryTest {

    @Autowired
    private ObjectFactory<BlackDuckVulnerabilityTopicCollector> vulnerabilityTopicCollectorFactory;
    @Autowired
    private ObjectFactory<BlackDuckPolicyTopicCollector> policyTopicCollectorFactory;

    @Test
    public void testCollectorCreation() {
        final BlackDuckTopicCollectorFactory topicCollectorFactory = new BlackDuckTopicCollectorFactory(vulnerabilityTopicCollectorFactory, policyTopicCollectorFactory);
        final Set<TopicCollector> topicCollectorSet = topicCollectorFactory.createTopicCollectors();
        assertFalse(topicCollectorSet.isEmpty());
        assertEquals(2, topicCollectorSet.size());
        final Set<TopicCollector> differentReferenceTopicCollectorSet = topicCollectorFactory.createTopicCollectors();
        assertFalse(differentReferenceTopicCollectorSet.isEmpty());
        assertEquals(2, differentReferenceTopicCollectorSet.size());

        // make sure they are different object references since TopicCollector does implement equals or hashcode which is ok. we want different instances.
        assertFalse(topicCollectorSet.equals(differentReferenceTopicCollectorSet));
    }
}
