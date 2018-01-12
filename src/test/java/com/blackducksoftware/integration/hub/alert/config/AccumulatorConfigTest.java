package com.blackducksoftware.integration.hub.alert.config;

import org.springframework.batch.core.launch.support.SimpleJobLauncher;

import com.blackducksoftware.integration.hub.alert.accumulator.AccumulatorProcessor;
import com.blackducksoftware.integration.hub.alert.accumulator.AccumulatorReader;
import com.blackducksoftware.integration.hub.alert.accumulator.AccumulatorWriter;

public class AccumulatorConfigTest extends CommonConfigTest<AccumulatorReader, AccumulatorWriter, AccumulatorProcessor, AccumulatorConfig> {

    @Override
    public String getJobName() {
        return "AccumulatorJob";
    }

    @Override
    public String getStepName() {
        return "AccumulatorStep";
    }

    @Override
    public AccumulatorConfig getConfigWithNullParams() {
        return new AccumulatorConfig(null, null, null, null, null, null, null, null, null);
    }

    @Override
    public AccumulatorConfig getConfigWithPassedParams(final SimpleJobLauncher simpleJobLauncher) {
        return new AccumulatorConfig(simpleJobLauncher, null, null, null, null, null, null, null, null);
    }

    @Override
    public void testCreateStep() {
    }

}
