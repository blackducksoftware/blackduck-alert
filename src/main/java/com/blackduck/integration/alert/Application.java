package com.blackduck.integration.alert;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.blackduck.integration.alert.component.authentication.security.database.UserDatabaseService;

@EnableJpaRepositories(basePackages = { "com.blackduck.integration.alert.database", "com.blackduck.integration.alert.channel", "com.blackduck.integration.alert.api.oauth",
    "com.blackduck.integration.alert.authentication" })
@EnableTransactionManagement
@EnableBatchProcessing
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication(exclude = { BatchAutoConfiguration.class })
public class Application {

    @Autowired
    private UserDatabaseService userDatabaseService;

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).run(args);
    }

    @Bean
    public DaoAuthenticationProvider alertDatabaseAuthProvider(PasswordEncoder defaultPasswordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDatabaseService);
        provider.setPasswordEncoder(defaultPasswordEncoder);
        return provider;
    }
}
