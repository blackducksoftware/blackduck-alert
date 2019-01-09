package com.synopsys.integration.alert.common.provider;

import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;

public abstract class EmailHandler {
    public abstract FieldAccessor updateFieldAccessor(final AggregateMessageContent content, final FieldAccessor originalAccessor);

}
