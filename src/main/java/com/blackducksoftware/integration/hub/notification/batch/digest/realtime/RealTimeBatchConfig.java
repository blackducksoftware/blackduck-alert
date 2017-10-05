package com.blackducksoftware.integration.hub.notification.batch.digest.realtime;

import java.util.List;

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
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.hub.notification.batch.CommonBatchConfig;
import com.blackducksoftware.integration.hub.notification.datasource.entity.event.NotificationEntity;
import com.blackducksoftware.integration.hub.notification.datasource.repository.NotificationRepository;

@Configuration
public class RealTimeBatchConfig {
    private static final String ACCUMULATOR_STEP_NAME = "RealTimeBatchStep";
    private static final String ACCUMULATOR_JOB_NAME = "RealTimeBatchJob";

    private final SimpleJobLauncher jobLauncher;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final TaskExecutor taskExecutor;
    private final NotificationRepository notificationRepository;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public RealTimeBatchConfig(final SimpleJobLauncher jobLauncher, final JobBuilderFactory jobBuilderFactory, final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor,
            final NotificationRepository notificationRepository, final PlatformTransactionManager transactionManager) {
        this.jobLauncher = jobLauncher;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.taskExecutor = taskExecutor;
        this.notificationRepository = notificationRepository;
        this.transactionManager = transactionManager;
    }

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
        return stepBuilderFactory.get(ACCUMULATOR_STEP_NAME).<List<NotificationEntity>, Object> chunk(1).reader(getReader()).processor(getProcessor()).writer(getWriter()).taskExecutor(taskExecutor).transactionManager(transactionManager)
                .build();
    }

    public RealTimeItemReader getReader() {
        return new RealTimeItemReader(notificationRepository);
    }

    public RealTimeItemWriter getWriter() {
        return new RealTimeItemWriter();
    }

    public RealTimeItemProcessor getProcessor() {
        return new RealTimeItemProcessor();
    }
}
