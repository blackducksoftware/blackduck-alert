/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.system;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.common.persistence.model.SystemMessageModel;

public class MultiSystemMessageModel extends AlertSerializableModel {
    private final List<SystemMessageModel> systemMessages;

    public MultiSystemMessageModel(List<SystemMessageModel> systemMessages) {
        this.systemMessages = systemMessages;
    }

    public List<SystemMessageModel> getSystemMessages() {
        return systemMessages;
    }

}
