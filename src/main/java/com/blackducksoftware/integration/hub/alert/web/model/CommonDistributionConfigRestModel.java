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
package com.blackducksoftware.integration.hub.alert.web.model;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class CommonDistributionConfigRestModel extends ConfigRestModel {
    private static final long serialVersionUID = -4723009315760610084L;

    private String distributionConfigId;
    private String distributionType;
    private String name;
    private String frequency;
    private String notificationType;
    private String filterByProject;

    public CommonDistributionConfigRestModel() {
    }

    public CommonDistributionConfigRestModel(final String id, final String distributionConfigId, final String distributionType, final String name, final String frequency, final String notificationType, final String filterByProject) {
        super(id);
        this.distributionConfigId = distributionConfigId;
        this.distributionType = distributionType;
        this.name = name;
        this.frequency = frequency;
        this.notificationType = notificationType;
        this.filterByProject = filterByProject;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
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

    @Override
    public String toString() {
        final ReflectionToStringBuilder reflectionToStringBuilder = new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE);
        return reflectionToStringBuilder.build();
    }

}
