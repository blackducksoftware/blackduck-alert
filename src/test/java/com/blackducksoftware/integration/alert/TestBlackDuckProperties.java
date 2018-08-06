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
package com.blackducksoftware.integration.alert;

import java.util.Optional;

import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalBlackDuckRepository;
import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;

public class TestBlackDuckProperties extends BlackDuckProperties {
    private final TestAlertProperties testAlertProperties;
    private final TestProperties testProperties;
    private Integer blackDuckTimeout;
    private String blackDuckUrl;
    private boolean urlSet;

    public TestBlackDuckProperties(final TestAlertProperties alertProperties) {
        this(Mockito.mock(GlobalBlackDuckRepository.class), alertProperties);
    }

    public TestBlackDuckProperties(final GlobalBlackDuckRepository globalHubRepository, final TestAlertProperties alertProperties) {
        this(globalHubRepository, alertProperties, 400);
    }

    public TestBlackDuckProperties(final GlobalBlackDuckRepository globalHubRepository, final TestAlertProperties alertProperties, final Integer blackDuckTimeout) {
        super(globalHubRepository, alertProperties);
        this.blackDuckTimeout = blackDuckTimeout;
        this.testAlertProperties = alertProperties;
        testProperties = new TestProperties();
        setHubTimeout(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT)));
        testAlertProperties.setAlertTrustCertificate(Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT)));
    }

    public void setBlackDuckUrl(final String blackDuckUrl) {
        this.blackDuckUrl = blackDuckUrl;
        urlSet = true;
    }

    @Override
    public Optional<String> getBlackDuckUrl() {
        if (urlSet) {
            return Optional.ofNullable(blackDuckUrl);
        }
        return Optional.of(testProperties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));
    }

    @Override
    public Integer getBlackDuckTimeout() {
        return blackDuckTimeout;
    }

    public void setHubTimeout(final Integer blackDuckTimeout) {
        this.blackDuckTimeout = blackDuckTimeout;
    }

    @Override
    public Optional<RestConnection> createRestConnection(final IntLogger intLogger) throws AlertException {
        testAlertProperties.setAlertTrustCertificate(true);
        return super.createRestConnection(intLogger);
    }

    @Override
    public Optional<GlobalBlackDuckConfigEntity> getBlackDuckConfig() {
        return Optional.of(new GlobalBlackDuckConfigEntity(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT)), testProperties.getProperty(TestPropertyKey.TEST_HUB_API_KEY)));
    }

    @Override
    public HubServerConfig createBlackDuckServerConfig(final IntLogger logger, final int blackDuckTimeout, final String blackDuckUsername, final String blackDuckPassword) throws AlertException {
        return createHubServerConfigWithCredentials(logger);
    }

    public HubServerConfig createHubServerConfigWithCredentials(final IntLogger logger) throws NumberFormatException, AlertException {
        return super.createBlackDuckServerConfig(logger, Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT)), testProperties.getProperty(TestPropertyKey.TEST_USERNAME),
                testProperties.getProperty(TestPropertyKey.TEST_PASSWORD));
    }

    public HubServicesFactory createHubServicesFactoryWithCredential(final IntLogger logger) throws Exception {
        testAlertProperties.setAlertTrustCertificate(true);
        final HubServerConfig blackDuckServerConfig = createHubServerConfigWithCredentials(logger);
        final RestConnection restConnection = blackDuckServerConfig.createCredentialsRestConnection(logger);
        return new HubServicesFactory(restConnection);
    }

}
