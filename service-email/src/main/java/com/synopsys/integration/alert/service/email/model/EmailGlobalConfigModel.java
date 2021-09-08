/*
 * service-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email.model;

import java.util.Map;

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
    
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Boolean getAuth() {
        return auth;
    }

    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getAdditionalJavaMailProperties() {
        return additionalJavaMailProperties;
    }

    public void setAdditionalJavaMailProperties(Map<String, String> additionalJavaMailProperties) {
        this.additionalJavaMailProperties = additionalJavaMailProperties;
    }

}
