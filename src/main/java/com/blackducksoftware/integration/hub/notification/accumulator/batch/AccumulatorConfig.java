package com.blackducksoftware.integration.hub.notification.accumulator.batch;

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

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityRequestService;
import com.blackducksoftware.integration.hub.dataservice.notification.NotificationResults;
import com.blackducksoftware.integration.hub.notification.EngineProperties;
import com.blackducksoftware.integration.hub.notification.HubServiceWrapper;
import com.blackducksoftware.integration.hub.notification.batch.CommonBatchConfig;
import com.blackducksoftware.integration.hub.notification.datasource.repository.NotificationRepository;
import com.blackducksoftware.integration.hub.notification.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.service.HubResponseService;

@Configuration
@EnableBatchProcessing
public class AccumulatorConfig {

    private static final String ACCUMULATOR_STEP_NAME = "AccumulatorStep";
    private static final String ACCUMULATOR_JOB_NAME = "AccumulatorJob";

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

    @Autowired
    private HubServiceWrapper hubServiceWrapper;

    @Autowired
    private NotificationRepository notificationRepository;

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
        return stepBuilderFactory.get(ACCUMULATOR_STEP_NAME).<NotificationResults, DBStoreEvent> chunk(1).reader(getAccumulatorReader()).processor(getAccumulatorProcessor()).writer(getAccumulatorWriter()).taskExecutor(taskExecutor).build();
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

    public NotificationAccumulatorProcessor getNotificationProcessor() {
        final HubResponseService hubResponseService = hubServiceWrapper.getHubServicesFactory().createHubResponseService();
        final MetaService metaService = hubServiceWrapper.getHubServicesFactory().createMetaService();
        final VulnerabilityRequestService vulnerabilityRequestService = hubServiceWrapper.getHubServicesFactory().createVulnerabilityRequestService();
        final NotificationAccumulatorProcessor notificationProcessor = new NotificationAccumulatorProcessor(hubResponseService, vulnerabilityRequestService, metaService);
        return notificationProcessor;
    }

}
