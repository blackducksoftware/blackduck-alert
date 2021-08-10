/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import com.synopsys.integration.alert.common.rest.model.Config;

public class EmailGlobalConfigResponse extends Config {
    private static final long serialVersionUID = -3469352637107648653L;

    public String host;
    public String from;

    public Boolean auth;
    public String user;
    public String password;

    public Integer port;
    public Integer connectionTimeout;
    public Integer timeout;
    public Integer writeTimeout;
    public String localhost;
    public String localaddress;
    public Integer localport;
    public String ehlo;
    public String authMechanisms;
    public Boolean authLoginDisable;
    public Boolean authPlainDisable;
    public Boolean authDigestMd5Disable;
    public Boolean authNtlmDisable;
    public String authNtlmDomain;
    public Integer authNtlmFlags;
    public Boolean authXoauth2Disable;
    public String submitter;
    public String dsnNotify;
    public String dsnRet;
    public Boolean allow8bitmime;
    public Boolean sendPartial;

    public Boolean saslEnabled;
    public String saslMechanisms;
    public String saslAuthorizationId;
    public String saslRealm;
    public Boolean saslUseCanonicalHostname;

    public Boolean quitwait;
    public Boolean reportSuccess;

    public Boolean sslEnable;
    public Boolean sslCheckServerIdentity;
    public String sslTrust;
    public String sslProtocols;
    public String sslCipherSuites;

    public Boolean startTlsEnable;
    public Boolean startTlsRequired;

    public String proxyHost;
    public Integer proxyPort;
    public String socksHost;
    public Integer socksPort;
    public String mailExtension;
    public Boolean userSet;
    public Boolean noopStrict;

}
