package com.blackducksoftware.integration.hub.notification.accumulator.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;

import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.EngineProperties;
import com.blackducksoftware.integration.hub.notification.HubServiceWrapper;
import com.blackducksoftware.integration.hub.notification.exception.NotificationEngineException;

@Configuration
@EnableBatchProcessing
public class AccumulatorConfig {
    private final static Logger logger = LoggerFactory.getLogger(AccumulatorConfig.class);

    @Autowired
    private SimpleJobLauncher jobLauncher;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private EngineProperties engineProperties;

    @Scheduled(cron = "#{@accumulatorCronExpression}")
    public JobExecution perform() throws Exception {
        final JobParameters param = new JobParametersBuilder().addString("JobID", String.valueOf(System.currentTimeMillis())).toJobParameters();
        final JobExecution execution = jobLauncher.run(accumulatorJob(), param);
        return execution;
    }

    @Bean
    public String accumulatorCronExpression() {
        return engineProperties.getAccumulatorCron();
    }

    @Bean
    public Job accumulatorJob() {
        return jobBuilderFactory.get("accumulatorJob").incrementer(new RunIdIncrementer()).listener(getAccumulatorListener()).flow(accumulatorStep()).end().build();
    }

    @Bean
    public Step accumulatorStep() {
        return stepBuilderFactory.get("accumulatorStep").<NotificationResults, NotificationResults> chunk(1).reader(getAccumulatorReader()).processor(getAccumulatorProcessor()).writer(getAccumulatorWriter()).taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public HubServiceWrapper hubServiceWrapper() {
        final HubServiceWrapper wrapper = new HubServiceWrapper(engineProperties);
        try {
            wrapper.init();
        } catch (final NotificationEngineException ex) {
            logger.error("Error initializing the service wrapper", ex);
        }
        return wrapper;
    }

    @Bean
    public AccumulatorReader getAccumulatorReader() {
        return new AccumulatorReader(hubServiceWrapper());
    }

    @Bean
    public AccumulatorWriter getAccumulatorWriter() {
        return new AccumulatorWriter();
    }

    @Bean
    public AccumulatorProcessor getAccumulatorProcessor() {
        return new AccumulatorProcessor();
    }

    @Bean
    public AccumulatorListener getAccumulatorListener() {
        return new AccumulatorListener();
    }
}
