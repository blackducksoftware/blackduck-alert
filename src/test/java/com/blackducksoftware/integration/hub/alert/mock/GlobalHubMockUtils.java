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
package com.blackducksoftware.integration.hub.alert.mock;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepository;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHubConfigRestModel;
import com.google.gson.JsonObject;

public class GlobalHubMockUtils implements MockUtils<CommonDistributionConfigRestModel, GlobalHubConfigRestModel, DatabaseEntity, GlobalHubConfigEntity> {
    private final String hubUrl;
    private final String hubTimeout;
    private final String hubUsername;
    private final String hubPassword;
    private final String hubProxyHost;
    private final String hubProxyPort;
    private final String hubProxyUsername;
    private final String hubProxyPassword;
    private final String hubAlwaysTrustCertificate;
    private final String id;

    public GlobalHubMockUtils() {
        this("HubUrl", "444", "HubUsername", "HubPassword", "HubProxyHost", "555", "HubProxyUsername", "HubProxyPassword", "true", "1");
    }

    public GlobalHubMockUtils(final String hubUrl, final String hubTimeout, final String hubUsername, final String hubPassword, final String hubProxyHost, final String hubProxyPort, final String hubProxyUsername,
            final String hubProxyPassword, final String hubAlwaysTrustCertificate, final String id) {
        this.hubUrl = hubUrl;
        this.hubTimeout = hubTimeout;
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
        this.hubProxyHost = hubProxyHost;
        this.hubProxyPort = hubProxyPort;
        this.hubProxyUsername = hubProxyUsername;
        this.hubProxyPassword = hubProxyPassword;
        this.hubAlwaysTrustCertificate = hubAlwaysTrustCertificate;
        this.id = id;
    }

    public GlobalProperties createTestGlobalProperties() {
        final GlobalHubRepository mockedGlobalRepository = Mockito.mock(GlobalHubRepository.class);
        final GlobalSchedulingRepository mockedScheduledRepository = Mockito.mock(GlobalSchedulingRepository.class);
        return createTestGlobalProperties(mockedGlobalRepository, mockedScheduledRepository);
    }

    public GlobalProperties createTestGlobalProperties(final GlobalHubRepository globalRepository, final GlobalSchedulingRepository globalSchedulingRepository) {
        final TestGlobalProperties globalProperties = new TestGlobalProperties(globalRepository, globalSchedulingRepository);
        globalProperties.hubUrl = hubUrl;
        globalProperties.hubTrustCertificate = Boolean.valueOf(hubAlwaysTrustCertificate);
        globalProperties.hubProxyHost = hubProxyHost;
        globalProperties.hubProxyPort = hubProxyPort;
        globalProperties.hubProxyUsername = hubProxyUsername;
        globalProperties.hubProxyPassword = hubProxyPassword;
        return globalProperties;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public String getHubTimeout() {
        return hubTimeout;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public String getHubProxyHost() {
        return hubProxyHost;
    }

    public String getHubProxyPort() {
        return hubProxyPort;
    }

    public String getHubProxyUsername() {
        return hubProxyUsername;
    }

    public String getHubProxyPassword() {
        return hubProxyPassword;
    }

    public String getHubAlwaysTrustCertificate() {
        return hubAlwaysTrustCertificate;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public GlobalHubConfigRestModel createGlobalRestModel() {
        final GlobalHubConfigRestModel restModel = new GlobalHubConfigRestModel(id, hubUrl, hubTimeout, hubUsername, hubPassword, hubProxyHost, hubProxyPort, hubProxyUsername, hubProxyPassword, hubAlwaysTrustCertificate);
        return restModel;
    }

    @Override
    public GlobalHubConfigRestModel createEmptyGlobalRestModel() {
        return new GlobalHubConfigRestModel();
    }

    @Override
    public GlobalHubConfigEntity createGlobalEntity() {
        final GlobalHubConfigEntity entity = new GlobalHubConfigEntity(Integer.valueOf(hubTimeout), hubUsername, hubPassword);
        entity.setId(Long.valueOf(id));
        return entity;
    }

    @Override
    public GlobalHubConfigEntity createEmptyGlobalEntity() {
        return new GlobalHubConfigEntity();
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("hubUrl", hubUrl);
        json.addProperty("hubTimeout", hubTimeout);
        json.addProperty("hubUsername", hubUsername);
        json.addProperty("hubProxyHost", hubProxyHost);
        json.addProperty("hubProxyPort", hubProxyPort);
        json.addProperty("hubProxyUsername", hubProxyUsername);
        json.addProperty("hubAlwaysTrustCertificate", hubAlwaysTrustCertificate);
        json.addProperty("id", id);
        return json.toString();
    }

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.add("hubUrl", null);
        json.add("hubTimeout", null);
        json.add("hubUsername", null);
        json.add("hubProxyHost", null);
        json.add("hubProxyPort", null);
        json.add("hubProxyUsername", null);
        json.add("hubAlwaysTrustCertificate", null);
        json.add("id", null);
        return json.toString();
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("hubTimeout", Integer.valueOf(hubTimeout));
        json.addProperty("hubUsername", hubUsername);
        json.addProperty("id", Long.valueOf(id));
        return json.toString();
    }

    @Override
    public String getEmptyGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.add("hubTimeout", null);
        json.add("hubUsername", null);
        json.add("id", null);
        return json.toString();
    }

    /*
     * Does not support the following
     */

    @Override
    @Deprecated
    public CommonDistributionConfigRestModel createRestModel() {
        return null;
    }

    @Override
    @Deprecated
    public CommonDistributionConfigRestModel createEmptyRestModel() {
        return null;
    }

    @Override
    @Deprecated
    public DatabaseEntity createEntity() {
        return null;
    }

    @Override
    @Deprecated
    public DatabaseEntity createEmptyEntity() {
        return null;
    }

    @Override
    @Deprecated
    public String getRestModelJson() {
        return null;
    }

    @Override
    @Deprecated
    public String getEmptyRestModelJson() {
        return null;
    }

    @Override
    @Deprecated
    public String getEntityJson() {
        return null;
    }

    @Override
    @Deprecated
    public String getEmptyEntityJson() {
        return null;
    }

    /*
     * End
     */

}
