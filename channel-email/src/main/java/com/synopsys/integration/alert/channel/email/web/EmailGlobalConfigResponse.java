/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.web;

import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class EmailGlobalConfigResponse extends AlertSerializableModel {
    private String host;
    private String from;

    private Boolean auth;
    private String user;
    private String password;

    private Integer port;
    private Integer connectionTimeout;
    private Integer timeout;
    private Integer writeTimeout;
    private String localhost;
    private String localaddress;
    private Integer localport;
    private String ehlo;
    private String authMechanisms;
    private Boolean authLoginDisable;
    private Boolean authPlainDisable;
    private Boolean authDigestMd5Disable;
    private Boolean authNtlmDisable;
    private String authNtlmDomain;
    private Integer authNtlmFlags;
    private Boolean authXoauth2Disable;
    private String submitter;
    private String dnsNotify;
    private String dnsRet;
    private Boolean allow8bitmime;
    private Boolean sendPartial;

    private Boolean saslEnabled;
    private String saslMechanisms;
    private String saslAuthorizationId;
    private String saslRealm;
    private Boolean saslUseCanonicalHostname;

    private Boolean quitwait;
    private Boolean reportSuccess;

    private Boolean sslEnable;
    private Boolean sslCheckServerIdentity;
    private String sslTrust;
    private String sslProtocols;
    private String sslCipherSuites;

    private Boolean startTlsEnable;
    private Boolean startTlsRequired;

    private String proxyHost;
    private Integer proxyPort;
    private String socksHost;
    private Integer socksPort;
    private String mailExtension;
    private Boolean userSet;
    private Boolean nmoopStrict;

    public Optional<String> getHost() {
        return Optional.ofNullable(host);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Optional<String> getFrom() {
        return Optional.ofNullable(from);
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Optional<Boolean> getAuth() {
        return Optional.ofNullable(auth);
    }

    public void setAuth(Boolean auth) {
        this.auth = auth;
    }

    public Optional<String> getUser() {
        return Optional.ofNullable(user);
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Optional<Integer> getPort() {
        return Optional.ofNullable(port);
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Optional<Integer> getConnectionTimeout() {
        return Optional.ofNullable(connectionTimeout);
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public Optional<Integer> getTimeout() {
        return Optional.ofNullable(timeout);
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Optional<Integer> getWriteTimeout() {
        return Optional.ofNullable(writeTimeout);
    }

    public void setWriteTimeout(Integer writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public Optional<String> getLocalhost() {
        return Optional.ofNullable(localhost);
    }

    public void setLocalhost(String localhost) {
        this.localhost = localhost;
    }

    public Optional<String> getLocaladdress() {
        return Optional.ofNullable(localaddress);
    }

    public void setLocaladdress(String localaddress) {
        this.localaddress = localaddress;
    }

    public Optional<Integer> getLocalport() {
        return Optional.ofNullable(localport);
    }

    public void setLocalport(Integer localport) {
        this.localport = localport;
    }

    public Optional<String> getEhlo() {
        return Optional.ofNullable(ehlo);
    }

    public void setEhlo(String ehlo) {
        this.ehlo = ehlo;
    }

    public Optional<String> getAuthMechanisms() {
        return Optional.ofNullable(authMechanisms);
    }

    public void setAuthMechanisms(String authMechanisms) {
        this.authMechanisms = authMechanisms;
    }

    public Optional<Boolean> getAuthLoginDisable() {
        return Optional.ofNullable(authLoginDisable);
    }

    public void setAuthLoginDisable(Boolean authLoginDisable) {
        this.authLoginDisable = authLoginDisable;
    }

    public Optional<Boolean> getAuthPlainDisable() {
        return Optional.ofNullable(authPlainDisable);
    }

    public void setAuthPlainDisable(Boolean authPlainDisable) {
        this.authPlainDisable = authPlainDisable;
    }

    public Optional<Boolean> getAuthDigestMd5Disable() {
        return Optional.ofNullable(authDigestMd5Disable);
    }

    public void setAuthDigestMd5Disable(Boolean authDigestMd5Disable) {
        this.authDigestMd5Disable = authDigestMd5Disable;
    }

    public Optional<Boolean> getAuthNtlmDisable() {
        return Optional.ofNullable(authNtlmDisable);
    }

    public void setAuthNtlmDisable(Boolean authNtlmDisable) {
        this.authNtlmDisable = authNtlmDisable;
    }

    public Optional<String> getAuthNtlmDomain() {
        return Optional.ofNullable(authNtlmDomain);
    }

    public void setAuthNtlmDomain(String authNtlmDomain) {
        this.authNtlmDomain = authNtlmDomain;
    }

    public Optional<Integer> getAuthNtlmFlags() {
        return Optional.ofNullable(authNtlmFlags);
    }

    public void setAuthNtlmFlags(Integer authNtlmFlags) {
        this.authNtlmFlags = authNtlmFlags;
    }

    public Optional<Boolean> getAuthXoauth2Disable() {
        return Optional.ofNullable(authXoauth2Disable);
    }

    public void setAuthXoauth2Disable(Boolean authXoauth2Disable) {
        this.authXoauth2Disable = authXoauth2Disable;
    }

    public Optional<String> getSubmitter() {
        return Optional.ofNullable(submitter);
    }

    public void setSubmitter(String submitter) {
        this.submitter = submitter;
    }

    public Optional<String> getDnsNotify() {
        return Optional.ofNullable(dnsNotify);
    }

    public void setDnsNotify(String dnsNotify) {
        this.dnsNotify = dnsNotify;
    }

    public Optional<String> getDnsRet() {
        return Optional.ofNullable(dnsRet);
    }

    public void setDnsRet(String dnsRet) {
        this.dnsRet = dnsRet;
    }

    public Optional<Boolean> getAllow8bitmime() {
        return Optional.ofNullable(allow8bitmime);
    }

    public void setAllow8bitmime(Boolean allow8bitmime) {
        this.allow8bitmime = allow8bitmime;
    }

    public Optional<Boolean> getSendPartial() {
        return Optional.ofNullable(sendPartial);
    }

    public void setSendPartial(Boolean sendPartial) {
        this.sendPartial = sendPartial;
    }

    public Optional<Boolean> getSaslEnabled() {
        return Optional.ofNullable(saslEnabled);
    }

    public void setSaslEnabled(Boolean saslEnabled) {
        this.saslEnabled = saslEnabled;
    }

    public Optional<String> getSaslMechanisms() {
        return Optional.ofNullable(saslMechanisms);
    }

    public void setSaslMechanisms(String saslMechanisms) {
        this.saslMechanisms = saslMechanisms;
    }

    public Optional<String> getSaslAuthorizationId() {
        return Optional.ofNullable(saslAuthorizationId);
    }

    public void setSaslAuthorizationId(String saslAuthorizationId) {
        this.saslAuthorizationId = saslAuthorizationId;
    }

    public Optional<String> getSaslRealm() {
        return Optional.ofNullable(saslRealm);
    }

    public void setSaslRealm(String saslRealm) {
        this.saslRealm = saslRealm;
    }

    public Optional<Boolean> getSaslUseCanonicalHostname() {
        return Optional.ofNullable(saslUseCanonicalHostname);
    }

    public void setSaslUseCanonicalHostname(Boolean saslUseCanonicalHostname) {
        this.saslUseCanonicalHostname = saslUseCanonicalHostname;
    }

    public Optional<Boolean> getQuitwait() {
        return Optional.ofNullable(quitwait);
    }

    public void setQuitwait(Boolean quitwait) {
        this.quitwait = quitwait;
    }

    public Optional<Boolean> getReportSuccess() {
        return Optional.ofNullable(reportSuccess);
    }

    public void setReportSuccess(Boolean reportSuccess) {
        this.reportSuccess = reportSuccess;
    }

    public Optional<Boolean> getSslEnable() {
        return Optional.ofNullable(sslEnable);
    }

    public void setSslEnable(Boolean sslEnable) {
        this.sslEnable = sslEnable;
    }

    public Optional<Boolean> getSslCheckServerIdentity() {
        return Optional.ofNullable(sslCheckServerIdentity);
    }

    public void setSslCheckServerIdentity(Boolean sslCheckServerIdentity) {
        this.sslCheckServerIdentity = sslCheckServerIdentity;
    }

    public Optional<String> getSslTrust() {
        return Optional.ofNullable(sslTrust);
    }

    public void setSslTrust(String sslTrust) {
        this.sslTrust = sslTrust;
    }

    public Optional<String> getSslProtocols() {
        return Optional.ofNullable(sslProtocols);
    }

    public void setSslProtocols(String sslProtocols) {
        this.sslProtocols = sslProtocols;
    }

    public Optional<String> getSslCipherSuites() {
        return Optional.ofNullable(sslCipherSuites);
    }

    public void setSslCipherSuites(String sslCipherSuites) {
        this.sslCipherSuites = sslCipherSuites;
    }

    public Optional<Boolean> getStartTlsEnable() {
        return Optional.ofNullable(startTlsEnable);
    }

    public void setStartTlsEnable(Boolean startTlsEnable) {
        this.startTlsEnable = startTlsEnable;
    }

    public Optional<Boolean> getStartTlsRequired() {
        return Optional.ofNullable(startTlsRequired);
    }

    public void setStartTlsRequired(Boolean startTlsRequired) {
        this.startTlsRequired = startTlsRequired;
    }

    public Optional<String> getProxyHost() {
        return Optional.ofNullable(proxyHost);
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Optional<Integer> getProxyPort() {
        return Optional.ofNullable(proxyPort);
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public Optional<String> getSocksHost() {
        return Optional.ofNullable(socksHost);
    }

    public void setSocksHost(String socksHost) {
        this.socksHost = socksHost;
    }

    public Optional<Integer> getSocksPort() {
        return Optional.ofNullable(socksPort);
    }

    public void setSocksPort(Integer socksPort) {
        this.socksPort = socksPort;
    }

    public Optional<String> getMailExtension() {
        return Optional.ofNullable(mailExtension);
    }

    public void setMailExtension(String mailExtension) {
        this.mailExtension = mailExtension;
    }

    public Optional<Boolean> getUserSet() {
        return Optional.ofNullable(userSet);
    }

    public void setUserSet(Boolean userSet) {
        this.userSet = userSet;
    }

    public Optional<Boolean> getNmoopStrict() {
        return Optional.ofNullable(nmoopStrict);
    }

    public void setNmoopStrict(Boolean nmoopStrict) {
        this.nmoopStrict = nmoopStrict;
    }
}
