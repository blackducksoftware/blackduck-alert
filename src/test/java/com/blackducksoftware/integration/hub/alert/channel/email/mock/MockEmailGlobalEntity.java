/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.channel.email.mock;

import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalEntityUtil;
import com.google.gson.JsonObject;

public class MockEmailGlobalEntity extends MockGlobalEntityUtil<GlobalEmailConfigEntity> {
    private String mailSmtpHost;
    private String mailSmtpUser;
    private String mailSmtpPassword;
    private boolean mailSmtpPasswordIsSet;
    private Integer mailSmtpPort;
    private Integer mailSmtpConnectionTimeout;
    private Integer mailSmtpTimeout;
    private String mailSmtpFrom;
    private String mailSmtpLocalhost;
    private Boolean mailSmtpEhlo;
    private Boolean mailSmtpAuth;
    private String mailSmtpDnsNotify;
    private String mailSmtpDnsRet;
    private Boolean mailSmtpAllow8bitmime;
    private Boolean mailSmtpSendPartial;
    private Long id;

    public MockEmailGlobalEntity() {
        this("MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", false, 99, 400, 500, "MailSmtpFrom", "MailSmtpLocalhost", true, false, "MailSmtpDnsNotify", "MailSmtpDnsRet", true, false, 1L);
    }

    private MockEmailGlobalEntity(final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final boolean mailSmtpPasswordIsSet, final Integer mailSmtpPort, final Integer mailSmtpConnectionTimeout,
            final Integer mailSmtpTimeout, final String mailSmtpFrom, final String mailSmtpLocalhost, final Boolean mailSmtpEhlo, final Boolean mailSmtpAuth, final String mailSmtpDnsNotify, final String mailSmtpDnsRet,
            final Boolean mailSmtpAllow8bitmime, final Boolean mailSmtpSendPartial, final Long id) {
        super();
        this.mailSmtpHost = mailSmtpHost;
        this.mailSmtpUser = mailSmtpUser;
        this.mailSmtpPassword = mailSmtpPassword;
        this.mailSmtpPasswordIsSet = mailSmtpPasswordIsSet;
        this.mailSmtpPort = mailSmtpPort;
        this.mailSmtpConnectionTimeout = mailSmtpConnectionTimeout;
        this.mailSmtpTimeout = mailSmtpTimeout;
        this.mailSmtpFrom = mailSmtpFrom;
        this.mailSmtpLocalhost = mailSmtpLocalhost;
        this.mailSmtpEhlo = mailSmtpEhlo;
        this.mailSmtpAuth = mailSmtpAuth;
        this.mailSmtpDnsNotify = mailSmtpDnsNotify;
        this.mailSmtpDnsRet = mailSmtpDnsRet;
        this.mailSmtpAllow8bitmime = mailSmtpAllow8bitmime;
        this.mailSmtpSendPartial = mailSmtpSendPartial;
        this.id = id;
    }

    public String getMailSmtpHost() {
        return mailSmtpHost;
    }

    public String getMailSmtpUser() {
        return mailSmtpUser;
    }

    public String getMailSmtpPassword() {
        return mailSmtpPassword;
    }

    public boolean isMailSmtpPasswordIsSet() {
        return mailSmtpPasswordIsSet;
    }

    public Integer getMailSmtpPort() {
        return mailSmtpPort;
    }

    public Integer getMailSmtpConnectionTimeout() {
        return mailSmtpConnectionTimeout;
    }

    public Integer getMailSmtpTimeout() {
        return mailSmtpTimeout;
    }

    public String getMailSmtpFrom() {
        return mailSmtpFrom;
    }

    public String getMailSmtpLocalhost() {
        return mailSmtpLocalhost;
    }

    public Boolean getMailSmtpEhlo() {
        return mailSmtpEhlo;
    }

    public Boolean getMailSmtpAuth() {
        return mailSmtpAuth;
    }

    public String getMailSmtpDnsNotify() {
        return mailSmtpDnsNotify;
    }

    public String getMailSmtpDnsRet() {
        return mailSmtpDnsRet;
    }

    public Boolean getMailSmtpAllow8bitmime() {
        return mailSmtpAllow8bitmime;
    }

    public Boolean getMailSmtpSendPartial() {
        return mailSmtpSendPartial;
    }

    public void setMailSmtpHost(final String mailSmtpHost) {
        this.mailSmtpHost = mailSmtpHost;
    }

    public void setMailSmtpUser(final String mailSmtpUser) {
        this.mailSmtpUser = mailSmtpUser;
    }

    public void setMailSmtpPassword(final String mailSmtpPassword) {
        this.mailSmtpPassword = mailSmtpPassword;
    }

    public void setMailSmtpPasswordIsSet(final boolean mailSmtpPasswordIsSet) {
        this.mailSmtpPasswordIsSet = mailSmtpPasswordIsSet;
    }

    public void setMailSmtpPort(final Integer mailSmtpPort) {
        this.mailSmtpPort = mailSmtpPort;
    }

    public void setMailSmtpConnectionTimeout(final Integer mailSmtpConnectionTimeout) {
        this.mailSmtpConnectionTimeout = mailSmtpConnectionTimeout;
    }

    public void setMailSmtpTimeout(final Integer mailSmtpTimeout) {
        this.mailSmtpTimeout = mailSmtpTimeout;
    }

    public void setMailSmtpFrom(final String mailSmtpFrom) {
        this.mailSmtpFrom = mailSmtpFrom;
    }

    public void setMailSmtpLocalhost(final String mailSmtpLocalhost) {
        this.mailSmtpLocalhost = mailSmtpLocalhost;
    }

    public void setMailSmtpEhlo(final Boolean mailSmtpEhlo) {
        this.mailSmtpEhlo = mailSmtpEhlo;
    }

    public void setMailSmtpAuth(final Boolean mailSmtpAuth) {
        this.mailSmtpAuth = mailSmtpAuth;
    }

    public void setMailSmtpDnsNotify(final String mailSmtpDnsNotify) {
        this.mailSmtpDnsNotify = mailSmtpDnsNotify;
    }

    public void setMailSmtpDnsRet(final String mailSmtpDnsRet) {
        this.mailSmtpDnsRet = mailSmtpDnsRet;
    }

    public void setMailSmtpAllow8bitmime(final Boolean mailSmtpAllow8bitmime) {
        this.mailSmtpAllow8bitmime = mailSmtpAllow8bitmime;
    }

    public void setMailSmtpSendPartial(final Boolean mailSmtpSendPartial) {
        this.mailSmtpSendPartial = mailSmtpSendPartial;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public GlobalEmailConfigEntity createGlobalEntity() {
        final GlobalEmailConfigEntity entity = new GlobalEmailConfigEntity(mailSmtpHost, mailSmtpUser, mailSmtpPassword, Integer.valueOf(mailSmtpPort), Integer.valueOf(mailSmtpConnectionTimeout), Integer.valueOf(mailSmtpTimeout),
                mailSmtpFrom, mailSmtpLocalhost, Boolean.valueOf(mailSmtpEhlo), Boolean.valueOf(mailSmtpAuth), mailSmtpDnsNotify, mailSmtpDnsRet, Boolean.valueOf(mailSmtpAllow8bitmime), Boolean.valueOf(mailSmtpSendPartial));
        entity.setId(id);
        return entity;
    }

    @Override
    public GlobalEmailConfigEntity createEmptyGlobalEntity() {
        return new GlobalEmailConfigEntity();
    }

    @Override
    public String getGlobalEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("mailSmtpHost", mailSmtpHost);
        json.addProperty("mailSmtpUser", mailSmtpUser);
        json.addProperty("mailSmtpPort", mailSmtpPort);
        json.addProperty("mailSmtpConnectionTimeout", mailSmtpConnectionTimeout);
        json.addProperty("mailSmtpTimeout", mailSmtpTimeout);
        json.addProperty("mailSmtpFrom", mailSmtpFrom);
        json.addProperty("mailSmtpLocalhost", mailSmtpLocalhost);
        json.addProperty("mailSmtpEhlo", mailSmtpEhlo);
        json.addProperty("mailSmtpAuth", mailSmtpAuth);
        json.addProperty("mailSmtpDnsNotify", mailSmtpDnsNotify);
        json.addProperty("mailSmtpDnsRet", mailSmtpDnsRet);
        json.addProperty("mailSmtpAllow8bitmime", mailSmtpAllow8bitmime);
        json.addProperty("mailSmtpSendPartial", mailSmtpSendPartial);
        json.addProperty("id", id);
        return json.toString();
    }

}
