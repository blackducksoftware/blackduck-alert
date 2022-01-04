/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.system;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;

public class BaseSystemValidator {
    private SystemMessageAccessor systemMessageAccessor;

    public BaseSystemValidator(SystemMessageAccessor systemMessageAccessor) {
        this.systemMessageAccessor = systemMessageAccessor;
    }

    public boolean addSystemMessageForError(String errorMessage, SystemMessageSeverity systemMessageSeverity, SystemMessageType messageType, boolean hasError) {
        return addSystemMessageForError(errorMessage, systemMessageSeverity, messageType.name(), hasError);
    }

    public boolean addSystemMessageForError(String errorMessage, SystemMessageSeverity systemMessageSeverity, String messageType, boolean hasError) {
        if (hasError) {
            getSystemMessageAccessor().addSystemMessage(errorMessage, systemMessageSeverity, messageType);
            return true;
        }
        return false;
    }

    public void removeSystemMessagesByType(SystemMessageType messageType) {
        getSystemMessageAccessor().removeSystemMessagesByType(messageType);
    }

    public void removeSystemMessagesByTypeString(String systemMessageType) {
        getSystemMessageAccessor().removeSystemMessagesByTypeString(systemMessageType);
    }

    public SystemMessageAccessor getSystemMessageAccessor() {
        return systemMessageAccessor;
    }
}
