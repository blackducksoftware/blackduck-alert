package com.synopsys.integration.alert.common.event;

import javax.jms.MessageListener;

public interface AlertEventListener extends MessageListener {
    String getName();

    String getDestinationName();

}
