package com.synopsys.integration.alert;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.slack.SlackChannel;
import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.mock.entity.MockCommonDistributionEntity;
import com.synopsys.integration.alert.workflow.scheduled.PhoneHomeTask;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.phonehome.PhoneHomeCallable;
import com.synopsys.integration.phonehome.PhoneHomeRequestBody;
import com.synopsys.integration.phonehome.PhoneHomeService;
import com.synopsys.integration.test.annotation.HubConnectionTest;
import com.synopsys.integration.test.tool.TestLogger;

public class PhoneHomeTest {

    private static final Logger logger = LoggerFactory.getLogger(PhoneHomeTest.class);

    @Test
    @Category(HubConnectionTest.class)
    public void testProductVersion() throws AlertException, IOException {
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(new TestAlertProperties());
        final CommonDistributionRepository commonDistributionRepository = Mockito.mock(CommonDistributionRepository.class);
        Mockito.when(commonDistributionRepository.findAll()).thenReturn(createConfigEntities());
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        final String productVersion = "test";
        Mockito.when(aboutReader.getProductVersion()).thenReturn(productVersion);
        final PhoneHomeTask phoneHome = new PhoneHomeTask(null, globalProperties, aboutReader, commonDistributionRepository);

        try (final BlackduckRestConnection restConnection = globalProperties.createRestConnection(new TestLogger()).get()) {
            final HubServicesFactory hubServicesFactory = globalProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
            final PhoneHomeService phoneHomeService = hubServicesFactory.createPhoneHomeService(Executors.newSingleThreadExecutor());
            final Optional<PhoneHomeCallable> callable = phoneHome.createPhoneHomeCallable(hubServicesFactory);
            if (callable.isPresent()) {
                final PhoneHomeRequestBody body = callable.get().createPhoneHomeRequestBody();

                Assert.assertNotNull(body);
                Assert.assertEquals(productVersion, body.getArtifactVersion());
                Assert.assertEquals("blackduck-alert", body.getArtifactId());
            } else {
                Assert.fail();
            }
        }

    }

    @Test
    public void testChannelMetaData() throws AlertException, IOException {
        final TestBlackDuckProperties globalProperties = new TestBlackDuckProperties(new TestAlertProperties());
        final CommonDistributionRepository commonDistributionRepository = Mockito.mock(CommonDistributionRepository.class);
        Mockito.when(commonDistributionRepository.findAll()).thenReturn(createConfigEntities());
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        final String productVersion = "test";
        Mockito.when(aboutReader.getProductVersion()).thenReturn(productVersion);
        final PhoneHomeTask phoneHome = new PhoneHomeTask(null, globalProperties, aboutReader, commonDistributionRepository);
        try (final BlackduckRestConnection restConnection = globalProperties.createRestConnection(new TestLogger()).get()) {
            final HubServicesFactory hubServicesFactory = globalProperties.createBlackDuckServicesFactory(restConnection, new Slf4jIntLogger(logger));
            final Optional<PhoneHomeCallable> callable = phoneHome.createPhoneHomeCallable(hubServicesFactory);
            if (callable.isPresent()) {
                final PhoneHomeRequestBody.Builder builder = callable.get().createPhoneHomeRequestBodyBuilder();

                phoneHome.addChannelMetaData(builder);
                final Map<String, String> metaData = builder.getMetaData();

                Assert.assertNull(metaData.get("channel." + EmailGroupChannel.COMPONENT_NAME));
                Assert.assertEquals(String.valueOf(2), metaData.get("channel." + HipChatChannel.COMPONENT_NAME));
                Assert.assertEquals(String.valueOf(1), metaData.get("channel." + SlackChannel.COMPONENT_NAME));
            } else {
                Assert.fail();
            }
        }
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
