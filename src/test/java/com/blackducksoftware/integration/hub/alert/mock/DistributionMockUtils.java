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

import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;

public class DistributionMockUtils {
    private final String commonId;
    private final String distributionConfigId;
    private final String distributionType;
    private final String name;
    private final String frequency;
    private final String notificationType;
    private final String filterByProject;

    public DistributionMockUtils() {
        this("1");
    }

    public DistributionMockUtils(final String distributionConfigId) {
        this("2", distributionConfigId, "test_type", "Name", "1 1 1 1 1 1", "Bad", "true");
    }

    public DistributionMockUtils(final String id, final String distributionConfigId, final String distributionType, final String name, final String frequency, final String notificationType, final String filterByProject) {
        super();
        this.commonId = id;
        this.distributionConfigId = distributionConfigId;
        this.distributionType = distributionType;
        this.name = name;
        this.frequency = frequency;
        this.notificationType = notificationType;
        this.filterByProject = filterByProject;
    }

    public String getCommonId() {
        return commonId;
    }

    public String getDistributionConfigId() {
        return distributionConfigId;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public String getName() {
        return name;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public String getFilterByProject() {
        return filterByProject;
    }

    public String getEmptyDistributionRestModelJson() {
        return "\"distributionConfigId\":null,\"distributionType\":null,\"name\":null,\"frequency\":null,\"notificationType\":null,\"filterByProject\":null,\"id\":null";
    }

    public String getDistributionRestModelJson() {
        final StringBuilder json = new StringBuilder();
        json.append("\"distributionConfigId\":\"");
        json.append(distributionConfigId);
        json.append("\",\"distributionType\":\"");
        json.append(distributionType);
        json.append("\",\"name\":\"");
        json.append(name);
        json.append("\",\"frequency\":\"");
        json.append(frequency);
        json.append("\",\"notificationType\":\"");
        json.append(notificationType);
        json.append("\",\"filterByProject\":\"");
        json.append(filterByProject);
        json.append("\",\"id\":\"");
        json.append(commonId);
        return json.toString();
    }

    public CommonDistributionConfigEntity createDistributionConfigEntity() {
        final CommonDistributionConfigEntity configEntity = new CommonDistributionConfigEntity(Long.valueOf(distributionConfigId), distributionType, name, frequency, notificationType, Boolean.valueOf(filterByProject));
        configEntity.setId(Long.valueOf(commonId));
        return configEntity;
    }

}
