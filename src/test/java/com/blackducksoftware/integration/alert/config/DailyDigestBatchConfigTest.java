package com.blackducksoftware.integration.alert.config;

import org.mockito.Mockito;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.alert.common.digest.DailyItemReader;
import com.blackducksoftware.integration.alert.common.digest.DigestItemProcessor;
import com.blackducksoftware.integration.alert.common.digest.DigestItemWriter;

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
    public DailyDigestBatchConfig getConfigWithSimpleJobLauncher(final SimpleJobLauncher simpleJobLauncher) {
        return new DailyDigestBatchConfig(simpleJobLauncher, null, null, null, null, null, null, null, null);
    }

    @Override
    public DailyDigestBatchConfig getConfigWithTaskScheduler(final TaskScheduler taskScheduler) {
        return new DailyDigestBatchConfig(null, null, null, null, null, null, taskScheduler, null, null);
    }

    @Override
    public DailyDigestBatchConfig getConfigWithParams(final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor, final PlatformTransactionManager platformTransactionManager) {
        return new DailyDigestBatchConfig(null, null, stepBuilderFactory, taskExecutor, null, platformTransactionManager, null, null, null);
    }

    @Override
    public DailyItemReader getMockReader() {
        return Mockito.mock(DailyItemReader.class);
    }

    @Override
    public DigestItemProcessor getMockProcessor() {
        return Mockito.mock(DigestItemProcessor.class);
    }

    @Override
    public DigestItemWriter getMockWriter() {
        return Mockito.mock(DigestItemWriter.class);
    }

}
