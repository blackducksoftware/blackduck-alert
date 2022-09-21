package com.synopsys.integration.alert.channel.jira.server.validator;

import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
public class JiraServerGlobalConfigExistsValidatorTest {
    @Mock
    private JiraServerGlobalConfigAccessor mockJiraServerGlobalConfigAccessor;

    @Test
    void existsReturnsTrueOnAtLeastOneConfiguration() {
        Mockito.when(mockJiraServerGlobalConfigAccessor.getConfigurationCount()).thenReturn(1L);

        JiraServerGlobalConfigExistsValidator validator = new JiraServerGlobalConfigExistsValidator(
                ChannelKeys.JIRA_SERVER,
                mockJiraServerGlobalConfigAccessor
        );

        assertTrue(validator.exists());

        Mockito.when(mockJiraServerGlobalConfigAccessor.getConfigurationCount()).thenReturn(5L);
        assertTrue(validator.exists());
    }

    @Test
    void existsReturnsFalseOnNoConfiguration() {
        Mockito.when(mockJiraServerGlobalConfigAccessor.getConfigurationCount()).thenReturn(0L);

        JiraServerGlobalConfigExistsValidator validator = new JiraServerGlobalConfigExistsValidator(
                ChannelKeys.JIRA_SERVER,
                mockJiraServerGlobalConfigAccessor
        );

        assertFalse(validator.exists());
    }
}
