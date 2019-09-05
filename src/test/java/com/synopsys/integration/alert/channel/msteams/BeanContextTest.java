package com.synopsys.integration.alert.channel.msteams;

import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.ApplicationConfiguration;
import com.synopsys.integration.alert.channel.slack.SlackChannelKey;
import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.web.config.DescriptorProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { Application.class, ApplicationConfiguration.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@WebAppConfiguration
public class BeanContextTest {
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
