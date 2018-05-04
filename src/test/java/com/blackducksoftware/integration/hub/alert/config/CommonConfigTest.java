package com.blackducksoftware.integration.hub.alert.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.step.builder.AbstractTaskletStepBuilder;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.scheduler.JobScheduler;

public abstract class CommonConfigTest<R extends ItemReader<?>, W extends ItemWriter<?>, P extends ItemProcessor<?, ?>, C extends JobScheduler<R, P, W>> {

    private OutputLogger outputLogger;
    private GlobalProperties globalProperties;

    @Before
    public void init() throws IOException {
        outputLogger = new OutputLogger();
        globalProperties = new TestGlobalProperties();
    }

    @After
    public void cleanup() throws IOException {
        outputLogger.cleanup();
    }

    public GlobalProperties getGlobalProperties() {
        return globalProperties;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void testCreateStep() {
        final StepBuilderFactory stepBuilderFactory = Mockito.mock(StepBuilderFactory.class);
        final TaskExecutor taskExecutor = Mockito.mock(TaskExecutor.class);
        final PlatformTransactionManager transactionManager = Mockito.mock(PlatformTransactionManager.class);

        final C config = getConfigWithParams(stepBuilderFactory, taskExecutor, transactionManager);

        final R reader = getMockReader();
        final P processor = getMockProcessor();
        final W writer = getMockWriter();

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

    public abstract C getConfigWithParams(StepBuilderFactory stepBuilderFactory, TaskExecutor taskExecutor, PlatformTransactionManager platformTransactionManager);

    public abstract R getMockReader();

    public abstract P getMockProcessor();

    public abstract W getMockWriter();

    @Test
    public void testScheduleJobExecutionBlankCron() throws IOException {
        final TaskScheduler taskScheduler = Mockito.mock(TaskScheduler.class);
        final ScheduledFuture<?> future = Mockito.mock(ScheduledFuture.class);
        Mockito.doReturn(future).when(taskScheduler).schedule(Mockito.any(JobScheduler.class), Mockito.any(CronTrigger.class));
        // Mockito.when(taskScheduler.schedule(Mockito.any(CommonConfig.class), Mockito.any(CronTrigger.class))).thenReturn(future);
        final C config = getConfigWithTaskScheduler(taskScheduler);
        config.scheduleExecution("1 1 1 1 1 1");
        config.scheduleExecution("");

        assertTrue(outputLogger.isLineContainingText("Un-Scheduling "));
    }

    public abstract C getConfigWithTaskScheduler(TaskScheduler taskScheduler);

    @Test
    public void testScheduleJobExecutionException() throws IOException {
        final C config = getConfigWithNullParams();
        config.scheduleExecution("fail");

        assertTrue(outputLogger.isLineContainingText("IllegalArgumentException"));
    }

    @Test
    public void testTimeNull() {
        final C config = getConfigWithNullParams();
        final Long nullResult = config.getMillisecondsToNextRun();

        assertNull(nullResult);

        final String nullStringResult = config.getFormatedNextRunTime();

        assertNull(nullStringResult);
    }

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
        C config = getConfigWithSimpleJobLauncher(simpleJobLauncher);

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

    public abstract C getConfigWithSimpleJobLauncher(SimpleJobLauncher simpleJobLauncher);

    @Test
    public void runException() throws IOException {
        final C config = getConfigWithNullParams();
        config.run();

        assertTrue(outputLogger.isLineContainingText("Exception"));
    }

}
