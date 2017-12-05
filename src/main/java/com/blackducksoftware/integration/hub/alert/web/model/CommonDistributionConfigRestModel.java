/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

    public void setDistributionConfigId(final String distributionConfigId) {
        this.distributionConfigId = distributionConfigId;
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
