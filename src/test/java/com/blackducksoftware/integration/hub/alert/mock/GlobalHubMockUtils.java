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

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.web.model.global.GlobalHubConfigRestModel;

public class GlobalHubMockUtils {

    public GlobalProperties createTestGlobalProperties(final GlobalHubRepository globalRepository) {
        final TestGlobalProperties globalProperties = new TestGlobalProperties(globalRepository);
        globalProperties.hubUrl = "HubUrl";
        globalProperties.hubTrustCertificate = false;
        globalProperties.hubProxyHost = "HubProxyHost";
        globalProperties.hubProxyPort = "22";
        globalProperties.hubProxyUsername = "HubProxyUsername";
        globalProperties.hubProxyPassword = "HubProxyPassword";
        return globalProperties;
    }

    public String getGlobalHubConfigRestModelJson() {
        return "{\"hubUrl\":\"HubUrl\",\"hubTimeout\":\"11\",\"hubUsername\":\"HubUsername\",\"hubPassword\":\"HubPassword\",\"hubProxyHost\":\"HubProxyHost\",\"hubProxyPort\":\"22\",\"hubProxyUsername\":\"HubProxyUsername\",\"hubProxyPassword\":\"HubProxyPassword\",\"hubAlwaysTrustCertificate\":\"false\",\"accumulatorCron\":\"0 0/1 * 1/1 * *\",\"dailyDigestCron\":\"0 0/1 * 1/1 * *\",\"id\":\"1\"}";
    }

    public String getGlobalHubConfigEntityJson() {
        return "{\"hubUrl\":\"HubUrl\",\"hubTimeout\":11,\"hubUsername\":\"HubUsername\",\"hubPassword\":\"HubPassword\",\"hubProxyHost\":\"HubProxyHost\",\"hubProxyPort\":\"22\",\"hubProxyUsername\":\"HubProxyUsername\",\"hubProxyPassword\":\"HubProxyPassword\",\"hubAlwaysTrustCertificate\":false,\"accumulatorCron\":\"0 0/1 * 1/1 * *\",\"dailyDigestCron\":\"0 0/1 * 1/1 * *\",\"id\":1}";
    }

    public GlobalHubConfigRestModel createGlobalHubConfigRestModel() {
        final GlobalHubConfigRestModel restModel = new GlobalHubConfigRestModel("1", "HubUrl", "11", "HubUsername", "HubPassword", "HubProxyHost", "22", "HubProxyUsername", "HubProxyPassword", "false", "0 0/1 * 1/1 * *", "0 0/1 * 1/1 * *",
                "0 0 12 1/2 * *");
        return restModel;
    }

    public GlobalHubConfigRestModel createGlobalHubConfigMaskedRestModel() {
        final GlobalHubConfigRestModel restModel = new GlobalHubConfigRestModel("1", "HubUrl", "11", "HubUsername", null, "HubProxyHost", "22", "HubProxyUsername", null, "false", "0 0/1 * 1/1 * *", "0 0/1 * 1/1 * *", "0 0 12 1/2 * *");
        return restModel;
    }

    public GlobalHubConfigEntity createGlobalHubConfigEntity() {
        final GlobalHubConfigEntity configEntity = new GlobalHubConfigEntity(11, "HubUsername", "HubPassword", "0 0/1 * 1/1 * *", "0 0/1 * 1/1 * *", "0 0 12 1/2 * *");
        configEntity.setId(1L);
        return configEntity;
    }
}
