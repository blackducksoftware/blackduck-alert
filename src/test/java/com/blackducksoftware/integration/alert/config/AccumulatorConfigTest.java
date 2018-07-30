package com.blackducksoftware.integration.alert.config;

import org.mockito.Mockito;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.alert.provider.blackduck.accumulator.BlackDuckAccumulatorProcessor;
import com.blackducksoftware.integration.alert.provider.blackduck.accumulator.BlackDuckAccumulatorReader;
import com.blackducksoftware.integration.alert.provider.blackduck.accumulator.BlackDuckAccumulatorWriter;
import com.blackducksoftware.integration.alert.workflow.scheduled.AccumulatorTask;

public class AccumulatorConfigTest extends CommonConfigTest<BlackDuckAccumulatorReader, BlackDuckAccumulatorWriter, BlackDuckAccumulatorProcessor, AccumulatorTask> {

    @Override
    public String getJobName() {
        return "AccumulatorJob";
    }

    @Override
    public String getStepName() {
        return "AccumulatorStep";
    }

    @Override
    public AccumulatorTask getConfigWithNullParams() {
        return new AccumulatorTask(null, null, null, null, null, null, getGlobalProperties(), null, null, contentConverter);
    }

    @Override
    public AccumulatorTask getConfigWithSimpleJobLauncher(final SimpleJobLauncher simpleJobLauncher) {
        return new AccumulatorTask(simpleJobLauncher, null, null, null, null, null, getGlobalProperties(), null, null, contentConverter);
    }

    @Override
    public AccumulatorTask getConfigWithTaskScheduler(final TaskScheduler taskScheduler) {
        return new AccumulatorTask(null, null, null, null, null, null, getGlobalProperties(), taskScheduler, null, contentConverter);
    }

    @Override
    public AccumulatorTask getConfigWithParams(final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor, final PlatformTransactionManager platformTransactionManager) {
        return new AccumulatorTask(null, null, stepBuilderFactory, taskExecutor, null, platformTransactionManager, getGlobalProperties(), null, null, contentConverter);
    }

    @Override
    public BlackDuckAccumulatorReader getMockReader() {
        return Mockito.mock(BlackDuckAccumulatorReader.class);
    }

    @Override
    public BlackDuckAccumulatorProcessor getMockProcessor() {
        return Mockito.mock(BlackDuckAccumulatorProcessor.class);
    }

    @Override
    public BlackDuckAccumulatorWriter getMockWriter() {
        return Mockito.mock(BlackDuckAccumulatorWriter.class);
    }

}
