package com.synopsys.integration.alert.common.descriptor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.CommonTypeConverter;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.web.actions.ConfiguredProjectsActions;
import com.synopsys.integration.alert.web.actions.NotificationTypesActions;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

public class CommonTypeConverterTest {

    @Test
    public void populateCommonFieldsFromEntityTest() {
        final ConfiguredProjectsActions configuredProjectsActions = Mockito.mock(ConfiguredProjectsActions.class);
        final NotificationTypesActions notificationTypesActions = Mockito.mock(NotificationTypesActions.class);
        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());

        final String distributionConfigId = "12";
        final String distributionType = "Email";
        final String jobName = "Job Name";
        final String provider = "blackduck";
        final String frequencyType = "REAL_TIME";
        final String filterByProject = "false";
        final String projectNamePattern = "projectNamePattern";
        final String formatType = "DEFAULT";

        final CommonDistributionConfig channelConfig = new CommonDistributionConfig();
        final CommonDistributionConfigEntity commonEntity = new CommonDistributionConfigEntity(Long.valueOf(distributionConfigId), distributionType, jobName, provider, FrequencyType.valueOf(frequencyType), Boolean.valueOf(filterByProject),
            projectNamePattern, FormatType.valueOf(formatType));

        final CommonTypeConverter commonTypeConverter = new CommonTypeConverter(configuredProjectsActions, notificationTypesActions, contentConverter);
        commonTypeConverter.populateCommonFieldsFromEntity(channelConfig, commonEntity);

        assertEquals(distributionConfigId, channelConfig.getDistributionConfigId());
        assertEquals(distributionType, channelConfig.getDistributionType());
        assertEquals(jobName, channelConfig.getName());
        assertEquals(provider, channelConfig.getProviderName());
        assertEquals(frequencyType, channelConfig.getFrequency());
        assertEquals(filterByProject, channelConfig.getFilterByProject());
        assertEquals(projectNamePattern, channelConfig.getProjectNamePattern());
        assertEquals(formatType, channelConfig.getFormatType());
    }
}
