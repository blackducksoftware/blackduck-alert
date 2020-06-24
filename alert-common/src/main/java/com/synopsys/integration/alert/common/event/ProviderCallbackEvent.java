package com.synopsys.integration.alert.common.event;

import com.synopsys.integration.alert.common.message.model.ComponentItem;
import com.synopsys.integration.alert.common.message.model.ContentKey;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.issuetracker.enumeration.IssueOperation;

public class ProviderCallbackEvent extends AlertEvent {
    private final String callbackUrl;
    private final String notificationType;
    private final LinkableItem channelDestination;
    private final IssueOperation operation;
    private final String channelEventDescription;

    private final ContentKey providerContentKey;
    private final ComponentItem componentItem;

    public ProviderCallbackEvent(
        String destination,
        String callbackUrl,
        String notificationType,
        LinkableItem channelDestination,
        IssueOperation operation,
        String channelEventDescription,
        ContentKey providerContentKey,
        ComponentItem componentItem
    ) {
        super(destination);
        this.callbackUrl = callbackUrl;
        this.notificationType = notificationType;
        this.channelDestination = channelDestination;
        this.operation = operation;
        this.channelEventDescription = channelEventDescription;
        this.providerContentKey = providerContentKey;
        this.componentItem = componentItem;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public LinkableItem getChannelDestination() {
        return channelDestination;
    }

    public IssueOperation getOperation() {
        return operation;
    }

    public String getChannelEventDescription() {
        return channelEventDescription;
    }

    public ContentKey getProviderContentKey() {
        return providerContentKey;
    }

    public ComponentItem getComponentItem() {
        return componentItem;
    }

}
