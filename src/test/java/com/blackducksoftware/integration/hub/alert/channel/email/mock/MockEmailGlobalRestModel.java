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

import com.blackducksoftware.integration.hub.alert.channel.email.controller.global.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.hub.alert.mock.model.global.MockGlobalRestModelUtil;
import com.google.gson.JsonObject;

public class MockEmailGlobalRestModel extends MockGlobalRestModelUtil<GlobalEmailConfigRestModel> {
    private String mailSmtpHost;
    private String mailSmtpUser;
    private String mailSmtpPassword;
    private boolean mailSmtpPasswordIsSet;
    private String mailSmtpPort;
    private String mailSmtpConnectionTimeout;
    private String mailSmtpTimeout;
    private String mailSmtpFrom;
    private String mailSmtpLocalhost;
    private boolean mailSmtpEhlo;
    private boolean mailSmtpAuth;
    private String mailSmtpDnsNotify;
    private String mailSmtpDnsRet;
    private boolean mailSmtpAllow8bitmime;
    private boolean mailSmtpSendPartial;
    private String id;

    public MockEmailGlobalRestModel() {
        this("MailSmtpHost", "MailSmtpUser", "MailSmtpPassword", false, "99", "400", "500", "MailSmtpFrom", "MailSmtpLocalhost", true, false, "MailSmtpDnsNotify", "MailSmtpDnsRet", true, false, "1");
    }

    private MockEmailGlobalRestModel(final String mailSmtpHost, final String mailSmtpUser, final String mailSmtpPassword, final boolean mailSmtpPasswordIsSet, final String mailSmtpPort, final String mailSmtpConnectionTimeout,
            final String mailSmtpTimeout, final String mailSmtpFrom, final String mailSmtpLocalhost, final boolean mailSmtpEhlo, final boolean mailSmtpAuth, final String mailSmtpDnsNotify, final String mailSmtpDnsRet,
            final boolean mailSmtpAllow8bitmime, final boolean mailSmtpSendPartial, final String id) {
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

    public String getMailSmtpPort() {
        return mailSmtpPort;
    }

    public String getMailSmtpConnectionTimeout() {
        return mailSmtpConnectionTimeout;
    }

    public String getMailSmtpTimeout() {
        return mailSmtpTimeout;
    }

    public String getMailSmtpFrom() {
        return mailSmtpFrom;
    }

    public String getMailSmtpLocalhost() {
        return mailSmtpLocalhost;
    }

    public boolean getMailSmtpEhlo() {
        return mailSmtpEhlo;
    }

    public boolean getMailSmtpAuth() {
        return mailSmtpAuth;
    }

    public String getMailSmtpDnsNotify() {
        return mailSmtpDnsNotify;
    }

    public String getMailSmtpDnsRet() {
        return mailSmtpDnsRet;
    }

    public boolean getMailSmtpAllow8bitmime() {
        return mailSmtpAllow8bitmime;
    }

    public boolean getMailSmtpSendPartial() {
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

    public void setMailSmtpPort(final String mailSmtpPort) {
        this.mailSmtpPort = mailSmtpPort;
    }

    public void setMailSmtpConnectionTimeout(final String mailSmtpConnectionTimeout) {
        this.mailSmtpConnectionTimeout = mailSmtpConnectionTimeout;
    }

    public void setMailSmtpTimeout(final String mailSmtpTimeout) {
        this.mailSmtpTimeout = mailSmtpTimeout;
    }

    public void setMailSmtpFrom(final String mailSmtpFrom) {
        this.mailSmtpFrom = mailSmtpFrom;
    }

    public void setMailSmtpLocalhost(final String mailSmtpLocalhost) {
        this.mailSmtpLocalhost = mailSmtpLocalhost;
    }

    public void setMailSmtpEhlo(final boolean mailSmtpEhlo) {
        this.mailSmtpEhlo = mailSmtpEhlo;
    }

    public void setMailSmtpAuth(final boolean mailSmtpAuth) {
        this.mailSmtpAuth = mailSmtpAuth;
    }

    public void setMailSmtpDnsNotify(final String mailSmtpDnsNotify) {
        this.mailSmtpDnsNotify = mailSmtpDnsNotify;
    }

    public void setMailSmtpDnsRet(final String mailSmtpDnsRet) {
        this.mailSmtpDnsRet = mailSmtpDnsRet;
    }

    public void setMailSmtpAllow8bitmime(final boolean mailSmtpAllow8bitmime) {
        this.mailSmtpAllow8bitmime = mailSmtpAllow8bitmime;
    }

    public void setMailSmtpSendPartial(final boolean mailSmtpSendPartial) {
        this.mailSmtpSendPartial = mailSmtpSendPartial;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return Long.valueOf(id);
    }

    @Override
    public GlobalEmailConfigRestModel createGlobalRestModel() {
        final GlobalEmailConfigRestModel restModel = new GlobalEmailConfigRestModel(id, mailSmtpHost, mailSmtpUser, mailSmtpPassword, mailSmtpPasswordIsSet, mailSmtpPort, mailSmtpConnectionTimeout, mailSmtpTimeout, mailSmtpFrom,
                mailSmtpLocalhost, mailSmtpEhlo, mailSmtpAuth, mailSmtpDnsNotify, mailSmtpDnsRet, mailSmtpAllow8bitmime, mailSmtpSendPartial);
        return restModel;
    }

    @Override
    public GlobalEmailConfigRestModel createEmptyGlobalRestModel() {
        return new GlobalEmailConfigRestModel();
    }

    @Override
    public String getGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("mailSmtpHost", mailSmtpHost);
        json.addProperty("mailSmtpUser", mailSmtpUser);
        json.addProperty("mailSmtpPasswordIsSet", mailSmtpPasswordIsSet);
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

    @Override
    public String getEmptyGlobalRestModelJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("mailSmtpPasswordIsSet", false);
        json.addProperty("mailSmtpEhlo", false);
        json.addProperty("mailSmtpAuth", false);
        json.addProperty("mailSmtpAllow8bitmime", false);
        json.addProperty("mailSmtpSendPartial", false);
        return json.toString();
    }

}
