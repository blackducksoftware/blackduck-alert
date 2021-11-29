package com.synopsys.integration.alert.configuration;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQPrefetchPolicy;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActiveMQConfiguration implements ActiveMQConnectionFactoryCustomizer {

    @Override
    public void customize(ActiveMQConnectionFactory factory) {
        ActiveMQPrefetchPolicy prefetchPolicy = new ActiveMQPrefetchPolicy();
        // setup consumer queue policy so that 100 events need to be acknowledged before send more events to the consumer.
        prefetchPolicy.setQueuePrefetch(100);
        factory.setPrefetchPolicy(prefetchPolicy);
        factory.setProducerWindowSize();
    }

}
