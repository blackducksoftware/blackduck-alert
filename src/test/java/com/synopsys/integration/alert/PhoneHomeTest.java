package com.synopsys.integration.alert;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.mock.entity.MockCommonDistributionEntity;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;

public class PhoneHomeTest {

    private static final Logger logger = LoggerFactory.getLogger(PhoneHomeTest.class);

    @Test
    @Tag(TestTags.CUSTOM_BLACKDUCK_CONNECTION)
    public void testProductVersion() {
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(new TestAlertProperties());
        final CommonDistributionRepository commonDistributionRepository = Mockito.mock(CommonDistributionRepository.class);
        Mockito.when(commonDistributionRepository.findAll()).thenReturn(createConfigEntities());
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        final String productVersion = "test";
        Mockito.when(aboutReader.getProductVersion()).thenReturn(productVersion);
        final PhoneHomeTask phoneHome = new PhoneHomeTask(null, globalProperties, aboutReader, commonDistributionRepository);
        phoneHome.run();
    }

    private List<CommonDistributionConfigEntity> createConfigEntities() {
        final MockCommonDistributionEntity mockCommonDistributionEntity = new MockCommonDistributionEntity();
        final CommonDistributionConfigEntity entity1 = mockCommonDistributionEntity.createEntity();
        final CommonDistributionConfigEntity entity2 = mockCommonDistributionEntity.createEntity();
        mockCommonDistributionEntity.setDistributionType(SlackChannel.COMPONENT_NAME);
        final CommonDistributionConfigEntity entity3 = mockCommonDistributionEntity.createEntity();

        return Arrays.asList(entity1, entity2, entity3);
    }
}
