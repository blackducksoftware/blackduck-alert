package com.blackducksoftware.integration.hub.alert.channel;

public abstract class DistributionChannel<T> {

    public abstract void recieveMessage(T message);

}
