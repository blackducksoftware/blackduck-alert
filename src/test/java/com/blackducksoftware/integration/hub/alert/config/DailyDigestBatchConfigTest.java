package com.blackducksoftware.integration.hub.alert.config;

import static org.junit.Assert.assertNotNull;

import org.mockito.Mockito;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

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
    @SuppressWarnings("rawtypes")
    public void testCreateStep() {
        final StepBuilderFactory stepBuilderFactory = Mockito.mock(StepBuilderFactory.class);
        final TaskExecutor taskExecutor = Mockito.mock(TaskExecutor.class);
        final PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);

        final DailyDigestBatchConfig config = new DailyDigestBatchConfig(null, null, stepBuilderFactory, taskExecutor, null, transactionManager, null, null, null);

        final DailyItemReader reader = Mockito.mock(DailyItemReader.class);
        final DigestItemProcessor processor = Mockito.mock(DigestItemProcessor.class);
        final DigestItemWriter writer = Mockito.mock(DigestItemWriter.class);

        final StepBuilder stepBuilder = Mockito.mock(StepBuilder.class);
        final SimpleStepBuilder simpleStepBuilder = Mockito.mock(SimpleStepBuilder.class);
        final AbstractTaskletStepBuilder abstractTaskletStepBuilder = Mockito.mock(AbstractTaskletStepBuilder.class);

        Mockito.when(stepBuilderFactory.get(Mockito.anyString())).thenReturn(stepBuilder);
        Mockito.when(stepBuilder.chunk(Mockito.anyInt())).thenReturn(simpleStepBuilder);
        Mockito.when(simpleStepBuilder.reader(reader)).thenReturn(simpleStepBuilder);
        Mockito.when(simpleStepBuilder.processor(processor)).thenReturn(simpleStepBuilder);
        Mockito.when(simpleStepBuilder.writer(writer)).thenReturn(simpleStepBuilder);
        Mockito.when(simpleStepBuilder.taskExecutor(taskExecutor)).thenReturn(abstractTaskletStepBuilder);
        Mockito.when(abstractTaskletStepBuilder.transactionManager(transactionManager)).thenReturn(abstractTaskletStepBuilder);
        Mockito.when(abstractTaskletStepBuilder.build()).thenReturn(new TaskletStep());

        final Step step = config.createStep(reader, processor, writer);
        assertNotNull(step);
    }

}
