package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.deprecated.channel.slack.SlackDistributionRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class SlackDistributionUpgradeTestIT extends AlertIntegrationTest {

    @Autowired
    private SlackDistributionRepository repository;

    @Test
    public void slackUpgradeTest() {
        final CommonDistributionFieldCreator commonDistributionFieldCreator = Mockito.mock(CommonDistributionFieldCreator.class);
        Mockito.when(commonDistributionFieldCreator.createCommonFields(Mockito.anyString(), Mockito.anyLong())).thenReturn(new LinkedList<>());
        final SlackDistributionUpgrade slackDistributionUpgrade = new SlackDistributionUpgrade(repository, null, commonDistributionFieldCreator, new FieldCreatorUtil());
        final String webhook = "webhook";
        final String channelUsername = "channelUsername";
        final String channelName = "channelName";
        final SlackDistributionConfigEntity slackDistributionConfigEntity = new SlackDistributionConfigEntity(webhook, channelUsername, channelName);
        final SlackDistributionConfigEntity savedEntity = repository.save(slackDistributionConfigEntity);

        final Map<String, ConfigurationFieldModel> fieldModelMap = slackDistributionUpgrade.convertEntityToFieldList(savedEntity)
                                                                       .stream()
                                                                       .collect(Collectors.toMap(configuredField -> configuredField.getFieldKey(), Function.identity()));
        assertEquals(3, fieldModelMap.entrySet().size());
        assertEquals(webhook, fieldModelMap.get(SlackDescriptor.KEY_WEBHOOK).getFieldValue().orElse(""));
        assertEquals(channelUsername, fieldModelMap.get(SlackDescriptor.KEY_CHANNEL_USERNAME).getFieldValue().orElse(""));
        assertEquals(channelName, fieldModelMap.get(SlackDescriptor.KEY_CHANNEL_NAME).getFieldValue().orElse(""));
    }

}
