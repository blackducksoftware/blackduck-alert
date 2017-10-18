package com.blackducksoftware.integration.hub.alert;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories
@EnableTransactionManagement
public class DataSourceConfig {

    // USING SPRING BATCH HERE. For JPA to work need to configure the JPATransactionManager bean.
    // SEE SPRING ISSUE: https://jira.spring.io/browse/BATCH-2642
    // Stack Overflow: https://stackoverflow.com/questions/38287298/persist-issue-with-a-spring-batch-itemwriter-using-a-jpa-repository
    @Bean
    public DataSource dataSource() {
        final EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        final EmbeddedDatabase dataSource = builder.setType(EmbeddedDatabaseType.H2).addScript("db/create-notification-schema.sql").addScript("db/create-notification-db.sql").build();
        return dataSource;
    }

    @Bean
    @Primary
    public JpaTransactionManager jpaTransactionManager(final DataSource dataSource) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }
}
