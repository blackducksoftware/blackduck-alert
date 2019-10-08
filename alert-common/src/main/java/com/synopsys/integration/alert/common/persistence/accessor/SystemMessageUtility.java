package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Date;
import java.util.List;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;

public interface SystemMessageUtility {

    void addSystemMessage(final String message, final SystemMessageSeverity severity, final SystemMessageType messageType);

    void removeSystemMessagesByType(final SystemMessageType messageType);

    List<SystemMessageModel> getSystemMessages();

    List<SystemMessageModel> getSystemMessagesAfter(final Date date);

    List<SystemMessageModel> getSystemMessagesBefore(final Date date);

    List<SystemMessageModel> findBetween(final DateRange dateRange);

    void deleteSystemMessages(final List<SystemMessageModel> messagesToDelete);
}
