package com.blackducksoftware.integration.hub.alert.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;

import com.blackducksoftware.integration.hub.alert.OutputLogger;

public abstract class CommonConfigTest<R extends ItemReader<?>, W extends ItemWriter<?>, P extends ItemProcessor<?, ?>, C extends CommonConfig<R, P, W>> {

    private OutputLogger outputLogger;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    // TODO figure out this test
    // @Test
    // public void testCreateStep() {
    // final StepBuilderFactory stepBuilderFactory = Mockito.mock(StepBuilderFactory.class);
    // final StepBuilder stepBuilder = Mockito.mock(StepBuilder.class);
    // Mockito.when(stepBuilder.chunk(Mockito.anyInt())).thenReturn(new SimpleStepBuilder());
    // Mockito.when(stepBuilderFactory.get(Mockito.anyString())).thenReturn(stepBuilder);
    // final C config = getConfigWithPassedParams(stepBuilderFactory);
    //
    // final Step step = config.createStep(config.reader(), config.processor(), config.writer());
    // assertEquals(getStepName(), step.getName());
    // }

    // public abstract C getConfigWithPassedParams(StepBuilderFactory stepBuilderFactory);

    @Test
    public void testReader() {
        final C config = getConfigWithNullParams();
        final R reader = config.reader();

        assertNotNull(reader);
    }

    @Test
    public void testWriter() {
        final C config = getConfigWithNullParams();
        final W accumulatorWriter = config.writer();

        assertNotNull(accumulatorWriter);
    }

    @Test
    public void testProcessor() {
        final C config = getConfigWithNullParams();
        final P accumulatorProcessor = config.processor();

        assertNotNull(accumulatorProcessor);
    }

    @Test
    public void testGetJobName() {
        final C config = getConfigWithNullParams();
        final String constantJobName = getJobName();

        assertEquals(config.getJobName(), constantJobName);
    }

    public abstract String getJobName();

    @Test
    public void testGetStepName() {
        final C config = getConfigWithNullParams();
        final String constantStepName = getStepName();

        assertEquals(config.getStepName(), constantStepName);
    }

    public abstract String getStepName();

    public abstract C getConfigWithNullParams();

    @Test
    public void testRun() throws Exception {
        final SimpleJobLauncher simpleJobLauncher = Mockito.mock(SimpleJobLauncher.class);
        C config = getConfigWithPassedParams(simpleJobLauncher);

        final JobExecution jobExecution = Mockito.mock(JobExecution.class);
        final ExitStatus exitStatus = Mockito.mock(ExitStatus.class);
        Mockito.when(exitStatus.getExitCode()).thenReturn("exit");
        Mockito.when(exitStatus.getExitDescription()).thenReturn("description");
        Mockito.when(jobExecution.getExitStatus()).thenReturn(exitStatus);
        Mockito.when(simpleJobLauncher.run(Mockito.any(), Mockito.any())).thenReturn(jobExecution);

        config = Mockito.spy(config);
        final Job job = Mockito.mock(Job.class);
        Mockito.doReturn(job).when(config).createJob(Mockito.any(), Mockito.any(), Mockito.any());

        config.run();

        final boolean successfulRun = outputLogger.isLineContainingText("Job finished with status");
        assertTrue(successfulRun);
    }

    public abstract C getConfigWithPassedParams(SimpleJobLauncher simpleJobLauncher);

}
