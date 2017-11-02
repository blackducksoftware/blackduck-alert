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
package com.blackducksoftware.integration.hub.alert.datasource.relation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "slack_user", schema = "user")
public class SlackUserRelation extends DatabaseRelation {
    private static final long serialVersionUID = -5649425971831549437L;

    @Column(name = "slack_config_id")
    private final Long slackConfigId;

    public SlackUserRelation(final Long userConfidId, final Long slackConfigId) {
        super(userConfidId);
        this.slackConfigId = slackConfigId;
    }

    public Long getChannelConfigId() {
        return slackConfigId;
    }
}
