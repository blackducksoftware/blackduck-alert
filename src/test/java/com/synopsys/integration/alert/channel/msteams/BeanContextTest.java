package com.synopsys.integration.alert.channel.msteams;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.alert.web.common.descriptor.DescriptorProcessor;

//@ExtendWith(SpringExtension.class)
//@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class })
//@TestPropertySource(locations = "classpath:spring-test.properties")
//@WebAppConfiguration
//TODO the AlertIntegrationTest is wiring components correctly to avoid an exception wiring a JMX MBean for 'datasource' need to figure out why extending AlertIntegrationTest solves this.  I suspect the dependsOn solves the autowiring issue.
public class BeanContextTest extends AlertIntegrationTest {
    @Autowired
    DescriptorProcessor descriptorProcessor;

    @Autowired
    SlackChannelKey slackChannelKey;

    @Autowired
    MsTeamsKey msTeamsKey;

    @Test
    public void testContext() {
        ConfigurationAction slackConfigurationAction = descriptorProcessor.retrieveConfigurationAction(slackChannelKey.getUniversalKey()).get();
        ConfigurationAction msTeamsConfigurationAction = descriptorProcessor.retrieveConfigurationAction(msTeamsKey.getUniversalKey()).get();

        assertNotNull(slackConfigurationAction);
        assertNotNull(msTeamsConfigurationAction);
    }

}
