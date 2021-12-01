package com.synopsys.integration.alert.common.event;

import org.apache.activemq.broker.BrokerRegistry;
import org.apache.activemq.broker.BrokerService;

import com.synopsys.integration.wait.WaitJobTask;

class BrokerServiceWaitTask implements WaitJobTask {
    @Override
    public boolean isComplete() {
        return BrokerRegistry.getInstance().lookup(BrokerService.DEFAULT_BROKER_NAME) != null;
    }
}
