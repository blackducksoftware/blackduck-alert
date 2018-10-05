/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert;

import java.util.Optional;

import org.mockito.Mockito;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckRepositoryAccessor;
import com.synopsys.integration.blackduck.configuration.HubServerConfig;
import com.synopsys.integration.blackduck.rest.BlackduckRestConnection;
import com.synopsys.integration.blackduck.service.HubServicesFactory;
import com.synopsys.integration.log.IntLogger;

public class TestBlackDuckProperties extends BlackDuckProperties {
    private final TestAlertProperties testAlertProperties;
    private final TestProperties testProperties;
    private Integer blackDuckTimeout;
    private String blackDuckUrl;
    private boolean urlSet;

    public TestBlackDuckProperties(final TestAlertProperties alertProperties) {
        this(Mockito.mock(BlackDuckRepositoryAccessor.class), alertProperties);
    }

    public TestBlackDuckProperties(final BlackDuckRepositoryAccessor blackDuckRepositoryAccessor, final TestAlertProperties alertProperties) {
        this(blackDuckRepositoryAccessor, alertProperties, 400);
    }

    public TestBlackDuckProperties(final BlackDuckRepositoryAccessor blackDuckRepositoryAccessor, final TestAlertProperties alertProperties, final Integer blackDuckTimeout) {
        super(blackDuckRepositoryAccessor, alertProperties);
        this.blackDuckTimeout = blackDuckTimeout;
        this.testAlertProperties = alertProperties;
        testProperties = new TestProperties();
        setHubTimeout(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT)));
        testAlertProperties.setAlertTrustCertificate(Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT)));
    }

    @Override
    public Optional<String> getBlackDuckUrl() {
        if (urlSet) {
            return Optional.ofNullable(blackDuckUrl);
        }
        return Optional.of(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL));
    }

    public void setBlackDuckUrl(final String blackDuckUrl) {
        this.blackDuckUrl = blackDuckUrl;
        urlSet = true;
    }

    @Override
    public Integer getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public void setHubTimeout(final Integer blackDuckTimeout) {
        this.blackDuckTimeout = blackDuckTimeout;
    }

    @Override
    public Optional<BlackduckRestConnection> createRestConnection(final IntLogger intLogger) throws AlertException {
        testAlertProperties.setAlertTrustCertificate(true);
        return super.createRestConnection(intLogger);
    }

    @Override
    public Optional<GlobalBlackDuckConfigEntity> getBlackDuckConfig() {
        return Optional.of(new GlobalBlackDuckConfigEntity(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT)), testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_API_KEY),
            testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_URL)));
    }

    @Override
    public HubServerConfig createBlackDuckServerConfig(final IntLogger logger, final int blackDuckTimeout, final String blackDuckUsername, final String blackDuckPassword) throws AlertException {
        return createHubServerConfigWithCredentials(logger);
    }

    public HubServerConfig createHubServerConfigWithCredentials(final IntLogger logger) throws NumberFormatException, AlertException {
        return super.createBlackDuckServerConfig(logger, Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_TIMEOUT)), testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_USERNAME),
            testProperties.getProperty(TestPropertyKey.TEST_BLACKDUCK_PROVIDER_PASSWORD));
    }

    public HubServicesFactory createHubServicesFactoryWithCredential(final IntLogger logger) throws Exception {
        testAlertProperties.setAlertTrustCertificate(true);
        final HubServerConfig blackDuckServerConfig = createHubServerConfigWithCredentials(logger);
        final BlackduckRestConnection restConnection = blackDuckServerConfig.createCredentialsRestConnection(logger);
        return new HubServicesFactory(HubServicesFactory.createDefaultGson(), HubServicesFactory.createDefaultJsonParser(), restConnection, logger);
    }

}
