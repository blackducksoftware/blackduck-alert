/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

public class EmailChannelMessageModelTest {
    static String SUBJECT_LINE = "test-subjectLine";
    static String CONTENT = "test-content";
    static String PROVIDER_NAME = "test-providerName";
    static String PROVIDER_URL = "test-providerUrl";
    static LinkableItem PROJECT = new LinkableItem("test-label-project", "test-value-project");
    static ProjectMessage PROJECT_MESSAGE = ProjectMessage.projectStatusInfo(null, PROJECT, null);

    @Test
    void simpleReturnsExpectedTest() {
        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simple(SUBJECT_LINE, CONTENT, PROVIDER_NAME, PROVIDER_URL);

        assertEquals(SUBJECT_LINE, emailChannelMessageModel.getSubjectLine());
        assertEquals(CONTENT, emailChannelMessageModel.getContent());
        assertEquals(PROVIDER_NAME, emailChannelMessageModel.getProviderName());
        assertEquals(PROVIDER_URL, emailChannelMessageModel.getProviderUrl());
        assertEquals(EmailChannelMessageModel.FORMAT_SUMMARY, emailChannelMessageModel.getMessageFormat());
        assertTrue(emailChannelMessageModel.getProjectName().isEmpty());
        assertTrue(emailChannelMessageModel.getSource().isEmpty());
    }

    @Test
    void simpleProjectReturnsExpectedTest() {
        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.simpleProject(SUBJECT_LINE, CONTENT, PROVIDER_NAME, PROVIDER_URL, PROJECT_MESSAGE);

        assertEquals(SUBJECT_LINE, emailChannelMessageModel.getSubjectLine());
        assertEquals(CONTENT, emailChannelMessageModel.getContent());
        assertEquals(PROVIDER_NAME, emailChannelMessageModel.getProviderName());
        assertEquals(PROVIDER_URL, emailChannelMessageModel.getProviderUrl());
        assertEquals(EmailChannelMessageModel.FORMAT_SUMMARY, emailChannelMessageModel.getMessageFormat());
        assertEquals(Optional.of(PROJECT_MESSAGE.getProject().getValue()), emailChannelMessageModel.getProjectName());
        assertEquals(Optional.of(PROJECT_MESSAGE), emailChannelMessageModel.getSource());
    }

    @Test
    void standardProjectReturnsExpectedTest() {
        EmailChannelMessageModel emailChannelMessageModel = EmailChannelMessageModel.standardProject(SUBJECT_LINE, CONTENT, PROVIDER_NAME, PROVIDER_URL, PROJECT_MESSAGE);

        assertEquals(SUBJECT_LINE, emailChannelMessageModel.getSubjectLine());
        assertEquals(CONTENT, emailChannelMessageModel.getContent());
        assertEquals(PROVIDER_NAME, emailChannelMessageModel.getProviderName());
        assertEquals(PROVIDER_URL, emailChannelMessageModel.getProviderUrl());
        assertEquals(EmailChannelMessageModel.FORMAT_STANDARD, emailChannelMessageModel.getMessageFormat());
        assertEquals(Optional.of(PROJECT_MESSAGE.getProject().getValue()), emailChannelMessageModel.getProjectName());
        assertEquals(Optional.of(PROJECT_MESSAGE), emailChannelMessageModel.getSource());
    }
}
