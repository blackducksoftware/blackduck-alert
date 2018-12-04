package com.synopsys.integration.alert.provider.blackduck.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckEntity;
import com.synopsys.integration.alert.provider.blackduck.mock.MockGlobalBlackDuckRestModel;
import com.synopsys.integration.alert.web.controller.GlobalControllerTest;
import com.synopsys.integration.alert.web.model.Config;

public class GlobalBlackDuckConfigControllerTestIT extends GlobalControllerTest {

    @Autowired
    BlackDuckRepositoryAccessor blackDuckRepositoryAccessor;

    @Autowired
    BlackDuckProperties blackDuckProperties;

    @Autowired
    AlertProperties alertProperties;

    @Override
    public RepositoryAccessor getGlobalRepositoryAccessor() {
        return blackDuckRepositoryAccessor;
    }

    @Override
    public DatabaseEntity getGlobalEntity() {
        final MockGlobalBlackDuckEntity mockBlackDuckEntity = new MockGlobalBlackDuckEntity();
        return mockBlackDuckEntity.createGlobalEntity();
    }

    @Override
    public Config getGlobalConfig() {
        final MockGlobalBlackDuckRestModel mockBlackDuckRestModel = new MockGlobalBlackDuckRestModel();
        final String hubUrl = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL);
        final String timeout = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT);
        final String apiKey = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY);
        final String alwaysTrust = testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT);
        mockBlackDuckRestModel.setBlackDuckUrl(hubUrl);
        mockBlackDuckRestModel.setBlackDuckTimeout(timeout);
        mockBlackDuckRestModel.setBlackDuckApiKey(apiKey);
        mockBlackDuckRestModel.setBlackDuckAlwaysTrustCertificate(alwaysTrust);
        mockBlackDuckRestModel.setBlackDuckProxyHost(null);
        mockBlackDuckRestModel.setBlackDuckProxyPassword(null);
        mockBlackDuckRestModel.setBlackDuckProxyPasswordIsSet(false);
        mockBlackDuckRestModel.setBlackDuckProxyPort(null);
        mockBlackDuckRestModel.setBlackDuckProxyUsername(null);
        return mockBlackDuckRestModel.createGlobalRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/provider/provider_blackduck";
    }

    @Override
    public String getTestDestination() {
        return null;
    }

    @Override
    public void testTestConfig() throws Exception {
        ReflectionTestUtils.setField(alertProperties, "alertTrustCertificate", Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT)));
        super.testTestConfig();
        ReflectionTestUtils.setField(alertProperties, "alertTrustCertificate", false);
    }

}
