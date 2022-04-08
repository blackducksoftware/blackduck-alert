package com.synopsys.integration.alert.configuration;

public class ActiveMQConfigurationTest {

    /*
    @Test
    public void testUrlWithQueryParam() {
        String brokerUrl = "vm://localhost?broker.persistent=true";
        String expectedUrl = String.format("%s&%s", brokerUrl, ActiveMQConfiguration.BROKER_SPLIT_MEMORY_QUERY_PARAM);
        ActiveMQConfiguration activeMQConfiguration = new ActiveMQConfiguration();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        activeMQConfiguration.customize(connectionFactory);
        assertNotNull(connectionFactory.getPrefetchPolicy());
        assertEquals(ActiveMQConfiguration.QUEUE_PREFETCH_LIMIT, connectionFactory.getPrefetchPolicy().getQueuePrefetch());
        assertEquals(expectedUrl, connectionFactory.getBrokerURL());
    }

    @Test
    public void testUrlWithoutQueryParam() {
        String brokerUrl = "vm://localhost";
        String expectedUrl = String.format("%s?%s", brokerUrl, ActiveMQConfiguration.BROKER_SPLIT_MEMORY_QUERY_PARAM);
        ActiveMQConfiguration activeMQConfiguration = new ActiveMQConfiguration();
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL(brokerUrl);
        activeMQConfiguration.customize(connectionFactory);
        assertNotNull(connectionFactory.getPrefetchPolicy());
        assertEquals(ActiveMQConfiguration.QUEUE_PREFETCH_LIMIT, connectionFactory.getPrefetchPolicy().getQueuePrefetch());
        assertEquals(expectedUrl, connectionFactory.getBrokerURL());
    }
    */
    
}
