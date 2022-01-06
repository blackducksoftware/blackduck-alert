/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.time.OffsetDateTime;
import java.util.List;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;

public interface SystemMessageAccessor {

    void addSystemMessage(String message, SystemMessageSeverity severity, SystemMessageType messageType);

    void addSystemMessage(String message, SystemMessageSeverity severity, String messageType);

    void removeSystemMessagesByType(SystemMessageType messageType);

    void removeSystemMessagesByTypeString(String systemMessageType);

    List<SystemMessageModel> getSystemMessages();

    List<SystemMessageModel> getSystemMessagesAfter(OffsetDateTime date);

    List<SystemMessageModel> getSystemMessagesBefore(OffsetDateTime date);

    List<SystemMessageModel> findBetween(DateRange dateRange);

    void deleteSystemMessages(List<SystemMessageModel> messagesToDelete);

    int deleteSystemMessagesCreatedBefore(OffsetDateTime date);

}
