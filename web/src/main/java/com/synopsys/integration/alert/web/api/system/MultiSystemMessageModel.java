/*
 * web
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.system;

import java.util.List;

import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class MultiSystemMessageModel extends AlertSerializableModel {
    private List<SystemMessageModel> systemMessages;

    public MultiSystemMessageModel(List<SystemMessageModel> systemMessages) {
        this.systemMessages = systemMessages;
    }

    public List<SystemMessageModel> getSystemMessages() {
        return systemMessages;
    }
}
