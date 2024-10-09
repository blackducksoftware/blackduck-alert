package com.blackduck.integration.alert.database.user;

import java.time.OffsetDateTime;

import com.blackduck.integration.alert.database.BaseEntity;
import com.blackduck.integration.alert.database.DatabaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "alert", name = "users")
public class UserEntity extends BaseEntity implements DatabaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "username")
    private String userName;
    @Column(name = "password")
    private String password;
    @Column(name = "email_address")
    private String emailAddress;
    @Column(name = "expired")
    private boolean expired;
    @Column(name = "locked")
    private boolean locked;
    @Column(name = "password_expired")
    private boolean passwordExpired;
    @Column(name = "enabled")
    private boolean enabled;
    @Column(name = "auth_type")
    private Long authenticationType;
    @Column(name = "last_login")
    private OffsetDateTime lastLogin;
    @Column(name = "last_failed_login")
    private OffsetDateTime lastFailedLogin;
    @Column(name = "failed_login_attempts")
    private Long failedLoginAttempts;

    public UserEntity() {
        // JPA requires default constructor definitions
    }

    public UserEntity(
        String userName,
        String password,
        String emailAddress,
        Long authenticationType,
        OffsetDateTime lastLogin,
        OffsetDateTime lastFailedLogin,
        Long failedLoginAttempts
    ) {
        this.userName = userName;
        this.password = password;
        this.emailAddress = emailAddress;
        this.expired = false;
        this.locked = false;
        this.passwordExpired = false;
        this.enabled = true;
        this.authenticationType = authenticationType;
        this.lastLogin = lastLogin;
        this.lastFailedLogin = lastFailedLogin;
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public UserEntity(
        String userName,
        String password,
        String emailAddress,
        boolean expired,
        boolean locked,
        boolean passwordExpired,
        boolean enabled,
        Long authenticationType,
        OffsetDateTime lastLogin,
        OffsetDateTime lastFailedLogin,
        Long failedLoginAttempts
    ) {
        this.userName = userName;
        this.password = password;
        this.emailAddress = emailAddress;
        this.expired = expired;
        this.locked = locked;
        this.passwordExpired = passwordExpired;
        this.enabled = enabled;
        this.authenticationType = authenticationType;
        this.lastLogin = lastLogin;
        this.lastFailedLogin = lastFailedLogin;
        this.failedLoginAttempts = failedLoginAttempts;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return this.userName;
    }

    public String getPassword() {
        return this.password;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean isExpired() {
        return this.expired;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean isPasswordExpired() {
        return this.passwordExpired;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Long getAuthenticationType() {
        return authenticationType;
    }

    public OffsetDateTime getLastLogin() {
        return lastLogin;
    }

    public OffsetDateTime getLastFailedLogin() {
        return lastFailedLogin;
    }

    public Long getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
}
