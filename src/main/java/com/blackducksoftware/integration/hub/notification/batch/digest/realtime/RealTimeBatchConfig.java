package com.blackducksoftware.integration.hub.notification.batch.digest.realtime;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;

import com.blackducksoftware.integration.hub.notification.batch.CommonBatchConfig;

@Configuration
public class RealTimeBatchConfig {
    private static final String ACCUMULATOR_STEP_NAME = "RealTimeBatchStep";
    private static final String ACCUMULATOR_JOB_NAME = "RealTimeBatchJob";

    @Autowired
    private SimpleJobLauncher jobLauncher;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private TaskExecutor taskExecutor;

    @Scheduled(cron = "0 0/1 * 1/1 * *")
    public JobExecution perform() throws Exception {
        final JobParameters param = new JobParametersBuilder().addString(CommonBatchConfig.JOB_ID_PROPERTY_NAME, String.valueOf(System.currentTimeMillis())).toJobParameters();
        final JobExecution execution = jobLauncher.run(accumulatorJob(), param);
        return execution;
    }

    public Job accumulatorJob() {
        return jobBuilderFactory.get(ACCUMULATOR_JOB_NAME).incrementer(new RunIdIncrementer()).flow(accumulatorStep()).end().build();
    }

    public Step accumulatorStep() {
        return stepBuilderFactory.get(ACCUMULATOR_STEP_NAME).<Object, Object> chunk(1).reader(getReader()).processor(getProcessor()).writer(getWriter()).taskExecutor(taskExecutor).build();
    }

    public RealTimeItemReader getReader() {
        return new RealTimeItemReader();
    }

    public RealTimeItemWriter getWriter() {
        return new RealTimeItemWriter();
    }

    public RealTimeItemProcessor getProcessor() {
        return new RealTimeItemProcessor();
    }
}
