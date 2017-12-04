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

public class SlackDistributionRestModel extends CommonDistributionConfigRestModel {
    private static final long serialVersionUID = -3032738984577328749L;

    private String channelName;
    private String channelUsername;

    public SlackDistributionRestModel() {

    }

    public SlackDistributionRestModel(final String id, final String channelName, final String channelUsername, final String distributionConfigId, final String distributionType, final String name, final String frequency,
            final String notificationType, final String filterByProject) {
        super(id, distributionConfigId, distributionType, name, frequency, notificationType, filterByProject);
        this.channelName = channelName;
        this.channelUsername = channelUsername;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    @Override
    public String toString() {
        final ReflectionToStringBuilder reflectionToStringBuilder = new ReflectionToStringBuilder(this, RecursiveToStringStyle.JSON_STYLE);
        return reflectionToStringBuilder.toString();
    }

}
