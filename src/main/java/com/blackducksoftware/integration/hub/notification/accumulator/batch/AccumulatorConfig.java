package com.blackducksoftware.integration.hub.notification.accumulator.batch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.blackducksoftware.integration.hub.notification.EngineProperties;

@Configuration
@EnableScheduling
public class AccumulatorConfig {

    @Autowired
    private EngineProperties engineProperties;

}
