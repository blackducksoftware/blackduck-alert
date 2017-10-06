package com.blackducksoftware.integration.hub.notification.channel;

public abstract class DistributionChannel<T> {

    public abstract void recieveMessage(T message);

}
