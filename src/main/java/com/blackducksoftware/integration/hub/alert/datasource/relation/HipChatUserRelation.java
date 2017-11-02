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
@Table(name = "hipchat_user", schema = "user")
public class HipChatUserRelation extends DatabaseRelation {
    private static final long serialVersionUID = 4597057895951603701L;

    @Column(name = "hipchat_config_id")
    private final Long hipChatConfigId;

    public HipChatUserRelation(final Long userConfidId, final Long hipChatConfigId) {
        super(userConfidId);
        this.hipChatConfigId = hipChatConfigId;
    }

    public Long getChannelConfigId() {
        return hipChatConfigId;
    }
}
