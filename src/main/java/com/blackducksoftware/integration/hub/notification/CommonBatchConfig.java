package com.blackducksoftware.integration.hub.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;

import com.blackducksoftware.integration.hub.notification.exception.NotificationEngineException;
import com.google.gson.Gson;

@Configuration
@EnableScheduling
public class CommonBatchConfig {
    private final static Logger logger = LoggerFactory.getLogger(CommonBatchConfig.class);

    public static final String JOB_ID_PROPERTY_NAME = "JobID";

    private final EngineProperties engineProperties;

    @Autowired
    public CommonBatchConfig(final EngineProperties engineProperties) {
        this.engineProperties = engineProperties;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public MapJobRepositoryFactoryBean mapJobRepositoryFactory(final PlatformTransactionManager txManager) throws Exception {
        final MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(txManager);
        factory.afterPropertiesSet();
        return factory;
    }

    @Bean
    public JobRepository jobRepository(final MapJobRepositoryFactoryBean factory) throws Exception {
        return factory.getObject();
    }

    @Bean
    public SimpleJobLauncher jobLauncher(final JobRepository jobRepository) {
        final SimpleJobLauncher launcher = new SimpleJobLauncher();
        launcher.setJobRepository(jobRepository);
        return launcher;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        final TaskExecutor executor = new SyncTaskExecutor();
        return executor;
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
    public Gson gson(final HubServiceWrapper hubServiceWrapper) {
        return hubServiceWrapper.getHubServicesFactory().getRestConnection().gson;
    }

}
