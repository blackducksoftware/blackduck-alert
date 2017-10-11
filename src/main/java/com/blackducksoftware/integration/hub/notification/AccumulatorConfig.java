package com.blackducksoftware.integration.hub.notification;

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
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityRequestService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.batch.accumulator.AccumulatorProcessor;
import com.blackducksoftware.integration.hub.notification.batch.accumulator.AccumulatorReader;
import com.blackducksoftware.integration.hub.notification.batch.accumulator.AccumulatorWriter;
import com.blackducksoftware.integration.hub.notification.datasource.repository.NotificationRepository;
import com.blackducksoftware.integration.hub.notification.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.notification.processor.NotificationItemProcessor;
import com.blackducksoftware.integration.hub.service.HubResponseService;

@Configuration
@EnableBatchProcessing
public class AccumulatorConfig {

    private static final String ACCUMULATOR_STEP_NAME = "AccumulatorStep";
    private static final String ACCUMULATOR_JOB_NAME = "AccumulatorJob";

    private final SimpleJobLauncher jobLauncher;
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final TaskExecutor taskExecutor;
    private final NotificationRepository notificationRepository;
    private final PlatformTransactionManager transactionManager;
    private final EngineProperties engineProperties;
    private final HubServiceWrapper hubServiceWrapper;

    @Autowired
    public AccumulatorConfig(final EngineProperties engineProperties, final SimpleJobLauncher jobLauncher, final JobBuilderFactory jobBuilderFactory, final StepBuilderFactory stepBuilderFactory, final TaskExecutor taskExecutor,
            final NotificationRepository notificationRepository, final PlatformTransactionManager transactionManager, final HubServiceWrapper hubServiceWrapper) {
        this.jobLauncher = jobLauncher;
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.taskExecutor = taskExecutor;
        this.notificationRepository = notificationRepository;
        this.transactionManager = transactionManager;
        this.engineProperties = engineProperties;
        this.hubServiceWrapper = hubServiceWrapper;

    }

    @Scheduled(cron = "#{@accumulatorCronExpression}")
    public JobExecution perform() throws Exception {
        final JobParameters param = new JobParametersBuilder().addString(CommonBatchConfig.JOB_ID_PROPERTY_NAME, String.valueOf(System.currentTimeMillis())).toJobParameters();
        final JobExecution execution = jobLauncher.run(accumulatorJob(), param);
        return execution;
    }

    @Bean
    public String accumulatorCronExpression() {
        return engineProperties.getAccumulatorCron();
    }

    public Job accumulatorJob() {
        return jobBuilderFactory.get(ACCUMULATOR_JOB_NAME).incrementer(new RunIdIncrementer()).flow(accumulatorStep()).end().build();
    }

    public Step accumulatorStep() {
        return stepBuilderFactory.get(ACCUMULATOR_STEP_NAME).<NotificationResults, DBStoreEvent> chunk(1).reader(getAccumulatorReader()).processor(getAccumulatorProcessor()).writer(getAccumulatorWriter()).taskExecutor(taskExecutor)
                .transactionManager(transactionManager).build();
    }

    public AccumulatorReader getAccumulatorReader() {
        return new AccumulatorReader(hubServiceWrapper);
    }

    public AccumulatorWriter getAccumulatorWriter() {
        return new AccumulatorWriter(notificationRepository);
    }

    public AccumulatorProcessor getAccumulatorProcessor() {
        return new AccumulatorProcessor(getNotificationProcessor());
    }

    public NotificationItemProcessor getNotificationProcessor() {
        final HubResponseService hubResponseService = hubServiceWrapper.getHubServicesFactory().createHubResponseService();
        final MetaService metaService = hubServiceWrapper.getHubServicesFactory().createMetaService();
        final VulnerabilityRequestService vulnerabilityRequestService = hubServiceWrapper.getHubServicesFactory().createVulnerabilityRequestService();
        final NotificationItemProcessor notificationProcessor = new NotificationItemProcessor(hubResponseService, vulnerabilityRequestService, metaService);
        return notificationProcessor;
    }

}
