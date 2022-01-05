/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.system;

import java.util.List;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;

public class MultiSystemMessageModel extends AlertSerializableModel {
    private final List<SystemMessageModel> systemMessages;

    public MultiSystemMessageModel(List<SystemMessageModel> systemMessages) {
        this.systemMessages = systemMessages;
    }

    public List<SystemMessageModel> getSystemMessages() {
        return systemMessages;
    }

}
