package com.synopsys.integration.alert.web.api.system;

import java.util.List;

import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;

public class MultiSystemMessageModel {
    private List<SystemMessageModel> systemMessages;

    public MultiSystemMessageModel(List<SystemMessageModel> systemMessages) {
        this.systemMessages = systemMessages;
    }

    public List<SystemMessageModel> getSystemMessages() {
        return systemMessages;
    }
}
