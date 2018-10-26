package com.synopsys.integration.alert.database.system;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "alert", name = "system_status")
public class SystemStatus {
    @Column(name = "initialized_configuration")
    private boolean initialConfigurationPerformed;
    @Column(name = "startup_time")
    private Date startupTime;
    @Column(name = "startupErrors")
    private String startupErrors;

    public SystemStatus() {
        //JPA requires a default constructor
    }

    public SystemStatus(final boolean initialConfigurationPerformed, final Date startupTime, final String startupErrors) {
        this.initialConfigurationPerformed = initialConfigurationPerformed;
        this.startupTime = startupTime;
        this.startupErrors = startupErrors;
    }

    public boolean isInitialConfigurationPerformed() {
        return initialConfigurationPerformed;
    }

    public Date getStartupTime() {
        return startupTime;
    }

    public String getStartupErrors() {
        return startupErrors;
    }
}
