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

import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.common.exception.AlertException;
import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubRepository;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.google.gson.Gson;

public class TestGlobalProperties extends GlobalProperties {
    private final TestProperties testProperties;
    private Integer hubTimeout;
    private String hubApiKey;
    private String productVersionOverride;

    public TestGlobalProperties() {
        this(Mockito.mock(GlobalHubRepository.class));
    }

    public TestGlobalProperties(final GlobalHubRepository globalHubRepository) {
        this(globalHubRepository, 400);
    }

    public TestGlobalProperties(final GlobalHubRepository globalHubRepository, final Integer hubTimeout) {
        super(globalHubRepository, new Gson());
        this.hubTimeout = hubTimeout;

        testProperties = new TestProperties();
        setHubTimeout(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT)));
        setHubTrustCertificate(Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT)));
        setHubUrl(testProperties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));
    }

    @Override
    public Integer getHubTimeout() {
        return hubTimeout;
    }

    public void setHubTimeout(final Integer hubTimeout) {
        this.hubTimeout = hubTimeout;
    }

    @Override
    public Optional<String> getHubApiKey() {
        return Optional.ofNullable(hubApiKey);
    }

    public void setHubApiKey(final String hubApiKey) {
        this.hubApiKey = hubApiKey;
    }

    public void setProductVersionOverride(final String productVersionOverride) {
        this.productVersionOverride = productVersionOverride;
    }

    @Override
    public String getProductVersion() {
        if (StringUtils.isNotBlank(productVersionOverride)) {
            return productVersionOverride;
        } else {
            return super.getProductVersion();
        }
    }

    @Override
    public Optional<RestConnection> createRestConnection(final IntLogger intLogger) throws AlertException {
        setHubUrl(testProperties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));
        setHubTrustCertificate(true);
        return super.createRestConnection(intLogger);
    }

    @Override
    public Optional<GlobalHubConfigEntity> getHubConfig() {
        return Optional.of(new GlobalHubConfigEntity(Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT)), testProperties.getProperty(TestPropertyKey.TEST_HUB_API_KEY)));
    }

    @Override
    public HubServerConfig createHubServerConfig(final IntLogger logger, final int hubTimeout, final String hubUsername, final String hubPassword) throws AlertException {
        return createHubServerConfigWithCredentials(logger);
    }

    public HubServerConfig createHubServerConfigWithCredentials(final IntLogger logger) throws NumberFormatException, AlertException {
        return super.createHubServerConfig(logger, Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_HUB_TIMEOUT)), testProperties.getProperty(TestPropertyKey.TEST_USERNAME),
                testProperties.getProperty(TestPropertyKey.TEST_PASSWORD));
    }

    public HubServicesFactory createHubServicesFactoryWithCredential(final IntLogger logger) throws Exception {
        setHubUrl(testProperties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));
        setHubTrustCertificate(true);
        final HubServerConfig hubServerConfig = createHubServerConfigWithCredentials(logger);
        final RestConnection restConnection = hubServerConfig.createCredentialsRestConnection(logger);
        return new HubServicesFactory(restConnection);
    }

}
