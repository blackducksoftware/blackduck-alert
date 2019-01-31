/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.database;

public class H2StoredProcedureTest {

    // FIXME this must be hard-coded into the liquibase changelog before release
    public static final java.lang.Void migrateDistributionJobs(final java.sql.Connection connection) throws java.sql.SQLException {
        try (final java.sql.ResultSet commonDistributionConfig = connection.createStatement().executeQuery("SELECT * FROM ALERT.COMMON_DISTRIBUTION_CONFIG;")) {
            while (commonDistributionConfig.next()) {
                final java.lang.Integer commonConfigId = commonDistributionConfig.getInt("ID");
                final java.lang.Integer distributionConfigId = commonDistributionConfig.getInt("DISTRIBUTION_CONFIG_ID");
                final java.lang.String distributionType = commonDistributionConfig.getString("DISTRIBUTION_TYPE");
                final java.lang.Boolean filterByProject = commonDistributionConfig.getBoolean("FILTER_BY_PROJECT");
                final java.lang.String name = commonDistributionConfig.getString("NAME");
                final java.lang.String providerName = commonDistributionConfig.getString("PROVIDER_NAME");

                java.lang.String projectNamePattern = commonDistributionConfig.getString("PROJECT_NAME_PATTERN");
                if (null == projectNamePattern) {
                    projectNamePattern = "";
                }

                final java.lang.Integer formatType = commonDistributionConfig.getInt("FORMAT_TYPE");
                final String formatTypeString;
                if (1 == formatType) {
                    formatTypeString = "DEFAULT";
                } else {
                    formatTypeString = "DIGEST";
                }

                final java.lang.Integer frequency = commonDistributionConfig.getInt("FREQUENCY");
                final String frequencyString;
                if (1 == frequency) {
                    frequencyString = "DAILY";
                } else {
                    frequencyString = "REAL_TIME";
                }

                // Create provider config
                connection.createStatement().executeUpdate(
                    "INSERT INTO ALERT.DESCRIPTOR_CONFIGS (DESCRIPTOR_ID, CONTEXT_ID) VALUES (GET_ID_FOR_REGISTERED_DESCRIPTOR_NAME('provider_blackduck') , GET_ID_FOR_CONFIG_CONTEXT('DISTRIBUTION'));");

                // Create new job with provider config
                final java.util.UUID jobUUID = java.util.UUID.randomUUID();
                connection.createStatement().executeUpdate("INSERT INTO ALERT.CONFIG_GROUPS (CONFIG_ID, JOB_ID) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG() , '" + jobUUID.toString() + "');");

                // Add provider fields
                connection.createStatement()
                    .executeUpdate(
                        "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.common.filter.by.project'), " + filterByProject + ");");
                connection.createStatement()
                    .executeUpdate(
                        "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.common.project.name.pattern'), '" + projectNamePattern + "');");
                connection.createStatement()
                    .executeUpdate(
                        "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('provider.distribution.format.type'), '" + formatTypeString + "');");

                // Add provider notification types
                try (final java.sql.ResultSet distributionNotificationTypes = connection.createStatement().executeQuery(
                    "SELECT * FROM ALERT.DISTRIBUTION_NOTIFICATION_TYPES WHERE DISTRIBUTION_NOTIFICATION_TYPES.COMMON_DISTRIBUTION_CONFIG_ID = " + commonConfigId.toString() + ";")) {
                    while (distributionNotificationTypes.next()) {
                        final java.lang.String notificationTypeName = distributionNotificationTypes.getString("NOTIFICATION_TYPE");
                        connection.createStatement().executeUpdate(
                            "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), "
                                + "GET_FIELD_ID_FOR_SOURCE_KEY('provider.distribution.notification.types'), '" + notificationTypeName + "');");
                    }
                }

                // Add project names
                try (final java.sql.ResultSet projectNamesJoined = connection.createStatement().executeQuery(
                    "SELECT PROJECT_NAME FROM ALERT.DISTRIBUTION_PROJECT_RELATION JOIN ALERT.CONFIGURED_PROJECTS WHERE DISTRIBUTION_PROJECT_RELATION.COMMON_DISTRIBUTION_CONFIG_ID = " + commonConfigId.toString() + ";")) {
                    while (projectNamesJoined.next()) {
                        final java.lang.String projectNameForConfig = projectNamesJoined.getString("PROJECT_NAME");
                        connection.createStatement().executeUpdate(
                            "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) "
                                + "VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.common.configured.project'), '" + projectNameForConfig + "');");
                    }
                }

                // Create channel config
                connection.createStatement().executeUpdate(
                    "INSERT INTO ALERT.DESCRIPTOR_CONFIGS (DESCRIPTOR_ID, CONTEXT_ID) VALUES (GET_ID_FOR_REGISTERED_DESCRIPTOR_NAME('" + distributionType + "') , GET_ID_FOR_CONFIG_CONTEXT('DISTRIBUTION'));");

                // Add channel config to job
                connection.createStatement().executeUpdate("INSERT INTO ALERT.CONFIG_GROUPS (CONFIG_ID, JOB_ID) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG() , '" + jobUUID.toString() + "');");

                // Add common channel fields
                connection.createStatement().executeUpdate(
                    "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.common.name'), '" + name + "');");
                connection.createStatement().executeUpdate(
                    "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.common.channel.name'), '" + distributionType + "');");
                connection.createStatement().executeUpdate(
                    "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.common.provider.name'), '" + providerName + "');");
                connection.createStatement().executeUpdate(
                    "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.common.frequency'), '" + frequencyString + "');");

                // Add specific channel fields
                if ("channel_email".equals(distributionType)) {
                    try (final java.sql.ResultSet emailDistributionConfigs = connection.createStatement().executeQuery(
                        "SELECT * FROM ALERT.EMAIL_GROUP_DISTRIBUTION_CONFIG WHERE EMAIL_GROUP_DISTRIBUTION_CONFIG.ID = " + distributionConfigId.toString() + ";")) {
                        if (emailDistributionConfigs.next()) {
                            final java.lang.String subjectLine = emailDistributionConfigs.getString("EMAIL_SUBJECT_LINE");
                            final java.lang.Boolean projectOwnerOnly = emailDistributionConfigs.getBoolean("PROJECT_OWNER_ONLY");
                            connection.createStatement().executeUpdate(
                                "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('email.subject.line'), '" + subjectLine + "');");
                            connection.createStatement().executeUpdate(
                                "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('project.owner.only'), '" + projectOwnerOnly.toString() + "');");
                        }
                    }
                } else if ("channel_hipchat".equals(distributionType)) {
                    try (final java.sql.ResultSet hipChatDistribtionConfigs = connection.createStatement().executeQuery(
                        "SELECT * FROM ALERT.HIP_CHAT_DISTRIBUTION_CONFIG WHERE HIP_CHAT_DISTRIBUTION_CONFIG.ID = " + distributionConfigId.toString() + ";")) {
                        if (hipChatDistribtionConfigs.next()) {
                            final java.lang.String color = hipChatDistribtionConfigs.getString("COLOR");
                            final java.lang.Boolean notify = hipChatDistribtionConfigs.getBoolean("NOTIFY");
                            final java.lang.Integer roomId = hipChatDistribtionConfigs.getInt("ROOM_ID");
                            connection.createStatement().executeUpdate(
                                "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.hipchat.color'), '" + color + "');");
                            connection.createStatement().executeUpdate(
                                "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.hipchat.notify'), '" + notify.toString() + "');");
                            connection.createStatement().executeUpdate(
                                "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.hipchat.room.id'), '" + roomId.toString() + "');");
                        }
                    }
                } else if ("channel_slack".equals(distributionType)) {
                    try (final java.sql.ResultSet slackDistributionConfigs = connection.createStatement().executeQuery(
                        "SELECT * FROM ALERT.SLACK_DISTRIBUTION_CONFIG WHERE SLACK_DISTRIBUTION_CONFIG.ID = " + distributionConfigId.toString() + ";")) {
                        if (slackDistributionConfigs.next()) {
                            final java.lang.String webhook = slackDistributionConfigs.getString("WEBHOOK");
                            final java.lang.String channelName = slackDistributionConfigs.getString("CHANNEL_NAME");
                            final java.lang.String channelUsername = slackDistributionConfigs.getString("CHANNEL_USERNAME");
                            connection.createStatement().executeUpdate(
                                "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.slack.webhook'), '" + webhook + "');");
                            connection.createStatement().executeUpdate(
                                "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.slack.channel.name'), '" + channelName + "');");
                            connection.createStatement().executeUpdate(
                                "INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (GET_LATEST_ID_FOR_DESCRIPTOR_CONFIG(), GET_FIELD_ID_FOR_SOURCE_KEY('channel.slack.channel.username'), '" + channelUsername + "');");
                        }
                    }
                } else {
                    // Unable to migrate unknown table
                    throw new java.sql.SQLException("Unable to migrate old distribution configuration");
                }

                // Update audit table
                try (final java.sql.ResultSet auditEntries = connection.createStatement().executeQuery("SELECT ID FROM ALERT.AUDIT_ENTRIES WHERE AUDIT_ENTRIES.COMMON_CONFIG_ID = " + commonConfigId.toString() + ";")) {
                    while (auditEntries.next()) {
                        final java.lang.Integer auditId = auditEntries.getInt("ID");
                        connection.createStatement().executeUpdate("UPDATE ALERT.AUDIT_ENTRIES SET AUDIT_ENTRIES.CONFIG_GROUP_ID = '" + jobUUID.toString() + "' WHERE AUDIT_ENTRIES.ID = " + auditId.toString() + ";");
                    }
                }
            }
        }
        return null;
    }
}
