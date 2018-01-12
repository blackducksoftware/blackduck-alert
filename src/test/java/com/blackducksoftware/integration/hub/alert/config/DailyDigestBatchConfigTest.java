package com.blackducksoftware.integration.hub.alert.config;

import org.springframework.batch.core.launch.support.SimpleJobLauncher;

import com.blackducksoftware.integration.hub.alert.digest.DailyItemReader;
import com.blackducksoftware.integration.hub.alert.digest.DigestItemProcessor;
import com.blackducksoftware.integration.hub.alert.digest.DigestItemWriter;

public class DailyDigestBatchConfigTest extends CommonConfigTest<DailyItemReader, DigestItemWriter, DigestItemProcessor, DailyDigestBatchConfig> {

    @Override
    public String getJobName() {
        return "DailyDigestBatchJob";
    }

    @Override
    public String getStepName() {
        return "DailyDigestBatchStep";
    }

    @Override
    public DailyDigestBatchConfig getConfigWithNullParams() {
        return new DailyDigestBatchConfig(null, null, null, null, null, null, null, null, null);
    }

    @Override
    public DailyDigestBatchConfig getConfigWithPassedParams(final SimpleJobLauncher simpleJobLauncher) {
        return new DailyDigestBatchConfig(simpleJobLauncher, null, null, null, null, null, null, null, null);
    }

    @Override
    public void testCreateStep() {
    }

}
