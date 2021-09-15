/*
 * service-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email.model;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;

public class EmailGlobalConfigModel {
    @JsonProperty("smtpFrom")
    private String from;
    @JsonProperty("smtpHost")
    private String host;
    @JsonProperty("smtpPort")
    private Integer port;

    @JsonProperty("smtpAuth")
    private Boolean auth;
    @JsonProperty("smtpUsername")
    private String username;
    @JsonProperty("smtpPassword")
    private String password;

    @JsonDeserialize(using = MapDeserializer.class)
    @JsonProperty("additionalJavaMailProperties")
    private Map<String, String> additionalJavaMailProperties;
    
    public Optional<String> getFrom() {
        return  Optional.ofNullable(from);
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(port);
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Optional<Boolean> getAuth() {
        return Optional.ofNullable(auth);
    }

    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    public Optional<String> getUsername() {
        return Optional.ofNullable(username);
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Optional<Map<String, String>> getAdditionalJavaMailProperties() {
        return Optional.ofNullable(additionalJavaMailProperties);
    }

    public void setAdditionalJavaMailProperties(Map<String, String> additionalJavaMailProperties) {
        this.additionalJavaMailProperties = additionalJavaMailProperties;
    }

}
