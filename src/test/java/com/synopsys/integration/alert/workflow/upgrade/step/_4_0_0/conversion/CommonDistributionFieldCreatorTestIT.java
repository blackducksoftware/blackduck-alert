package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.channel.slack.descriptor.SlackDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.CommonDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.channel.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.deprecated.channel.CommonDistributionRepository;
import com.synopsys.integration.alert.database.deprecated.channel.slack.SlackDistributionConfigEntity;
import com.synopsys.integration.alert.database.deprecated.channel.slack.SlackDistributionRepository;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class CommonDistributionFieldCreatorTestIT extends AlertIntegrationTest {

    @Autowired
    private FieldCreatorUtil fieldCreatorUtil;

    @Autowired
    private CommonDistributionRepository commonDistributionRepository;

    @Autowired
    private SlackDistributionRepository slackDistributionRepository;

    @Test
    public void createCommonFieldsTest() {
        final SlackDistributionConfigEntity slackDistributionConfigEntity = new SlackDistributionConfigEntity();
        final SlackDistributionConfigEntity savedEntity = slackDistributionRepository.save(slackDistributionConfigEntity);
        final Long entityId = savedEntity.getId();
        final CommonDistributionConfigEntity commonDistributionConfigEntity = new CommonDistributionConfigEntity(entityId, SlackChannel.COMPONENT_NAME, "test", BlackDuckProvider.COMPONENT_NAME, FrequencyType.DAILY, true, "",
            FormatType.DIGEST);
        commonDistributionRepository.save(commonDistributionConfigEntity);

        final CommonDistributionFieldCreator commonDistributionFieldCreator = new CommonDistributionFieldCreator(commonDistributionRepository, null, null, fieldCreatorUtil);
        final Map<String, ConfigurationFieldModel> fieldModelMap = commonDistributionFieldCreator.createCommonFields(SlackChannel.COMPONENT_NAME, entityId).stream()
                                                                       .collect(Collectors.toMap(config -> config.getFieldKey(), Function.identity()));

        assertEquals(4, fieldModelMap.entrySet().size());
        assertEquals("test", fieldModelMap.get(CommonDistributionUIConfig.KEY_NAME).getFieldValue().orElse(""));
        assertEquals(SlackChannel.COMPONENT_NAME, fieldModelMap.get(CommonDistributionUIConfig.KEY_CHANNEL_NAME).getFieldValue().orElse(""));
        assertNull(fieldModelMap.get(SlackDescriptor.KEY_WEBHOOK));
    }
}
