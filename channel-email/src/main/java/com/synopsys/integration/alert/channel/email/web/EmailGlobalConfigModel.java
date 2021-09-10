/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.synopsys.integration.alert.common.rest.model.Config;

public class EmailGlobalConfigModel extends Config {
    private static final long serialVersionUID = -3469352637107648653L;

    @JsonProperty("host")
    public String host;
    @JsonProperty("from")
    public String from;

    @JsonProperty("auth")
    public Boolean auth;
    @JsonProperty("user")
    public String user;
    @JsonProperty("password")
    public String password;

    @JsonProperty("port")
    public Integer port;
    @JsonProperty("connectionTimeout")
    public Integer connectionTimeout;
    @JsonProperty("timeout")
    public Integer timeout;
    @JsonProperty("writeTimeout")
    public Integer writeTimeout;
    @JsonProperty("localhost")
    public String localhost;
    @JsonProperty("localaddress")
    public String localaddress;
    @JsonProperty("localport")
    public Integer localport;
    @JsonProperty("ehlo")
    public String ehlo;
    @JsonProperty("authMechanisms")
    public String authMechanisms;
    @JsonProperty("authLoginDisable")
    public Boolean authLoginDisable;
    @JsonProperty("authPlainDisable")
    public Boolean authPlainDisable;
    @JsonProperty("authDigestMd5Disable")
    public Boolean authDigestMd5Disable;
    @JsonProperty("authNtlmDisable")
    public Boolean authNtlmDisable;
    @JsonProperty("authNtlmDomain")
    public String authNtlmDomain;
    @JsonProperty("authNtlmFlags")
    public Integer authNtlmFlags;
    @JsonProperty("authXoauth2Disable")
    public Boolean authXoauth2Disable;
    @JsonProperty("submitter")
    public String submitter;
    @JsonProperty("dsnNotify")
    public String dsnNotify;
    @JsonProperty("dsnRet")
    public String dsnRet;
    @JsonProperty("allow8bitmime")
    public Boolean allow8bitmime;
    @JsonProperty("sendPartial")
    public Boolean sendPartial;

    @JsonProperty("saslEnabled")
    public Boolean saslEnabled;
    @JsonProperty("saslMechanisms")
    public String saslMechanisms;
    @JsonProperty("saslAuthorizationId")
    public String saslAuthorizationId;
    @JsonProperty("saslRealm")
    public String saslRealm;
    @JsonProperty("saslUseCanonicalHostname")
    public Boolean saslUseCanonicalHostname;

    @JsonProperty("quitwait")
    public Boolean quitwait;
    @JsonProperty("reportSuccess")
    public Boolean reportSuccess;

    @JsonProperty("sslEnable")
    public Boolean sslEnable;
    @JsonProperty("sslCheckServerIdentity")
    public Boolean sslCheckServerIdentity;
    @JsonProperty("sslTrust")
    public String sslTrust;
    @JsonProperty("sslProtocols")
    public String sslProtocols;
    @JsonProperty("sslCipherSuites")
    public String sslCipherSuites;
    @JsonProperty("startTlsEnable")

    public Boolean startTlsEnable;
    @JsonProperty("startTlsRequired")
    public Boolean startTlsRequired;
    @JsonProperty("proxyHost")

    public String proxyHost;
    @JsonProperty("proxyPort")
    public Integer proxyPort;
    @JsonProperty("socksHost")
    public String socksHost;
    @JsonProperty("socksPort")
    public Integer socksPort;
    @JsonProperty("mailExtension")
    public String mailExtension;
    @JsonProperty("userSet")
    public Boolean userSet;
    @JsonProperty("noopStrict")
    public Boolean noopStrict;

}
