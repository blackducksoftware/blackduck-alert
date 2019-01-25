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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang3.StringUtils;

public final class H2StoredProcedures {
    public static final String UNIQUENESS_CONTRAINT_MESSAGE_SEGMENT = "Unique index or primary key violation";
    public static final String DOES_NOT_EXIST_MESSAGE = "No values returned for that query";

    public static void defineField(final Connection connection, final String fieldKey, final Boolean sensitive, final String descriptorName, final String context) throws SQLException {
        try (final Statement insertIntoDefinedFields = connection.createStatement()) {
            insertIntoDefinedFields.executeUpdate("INSERT INTO ALERT.DEFINED_FIELDS (SOURCE_KEY, SENSITIVE) VALUES ('" + fieldKey + "', " + sensitive + ");");
        } catch (final SQLException e) {
            ignoreUniquenessConstraintException(e);
        }

        final Integer fieldId = getFieldIdForSourceKey(connection, fieldKey);
        try (final Statement insertIntoFieldContexts = connection.createStatement()) {
            insertIntoFieldContexts.executeUpdate("INSERT INTO ALERT.FIELD_CONTEXTS (FIELD_ID, CONTEXT_ID) VALUES (" + fieldId + ", GET_ID_FOR_CONFIG_CONTEXT('" + StringUtils.upperCase(context) + "'));");
        } catch (final SQLException e) {
            ignoreUniquenessConstraintException(e);
        }
        try (final Statement insertIntoDescriptorFields = connection.createStatement()) {
            insertIntoDescriptorFields.executeUpdate("INSERT INTO ALERT.DESCRIPTOR_FIELDS (DESCRIPTOR_ID, FIELD_ID) VALUES (GET_ID_FOR_REGISTERED_DESCRIPTOR_NAME('" + StringUtils.lowerCase(descriptorName) + "'), " + fieldId + ");");
        }
    }

    public static Integer getIdForRegisteredDescriptorName(final Connection connection, final String descriptorName) throws SQLException {
        return getFirstInt(connection, "SELECT ID FROM ALERT.REGISTERED_DESCRIPTORS WHERE REGISTERED_DESCRIPTORS.NAME = '" + StringUtils.lowerCase(descriptorName) + "' LIMIT 1;", "ID");
    }

    public static Integer getIdForDescriptorType(final Connection connection, final String type) throws SQLException {
        return getFirstInt(connection, "SELECT ID FROM ALERT.DESCRIPTOR_TYPES WHERE DESCRIPTOR_TYPES.TYPE = '" + StringUtils.upperCase(type) + "' LIMIT 1;", "ID");
    }

    public static Integer getFieldIdForSourceKey(final Connection connection, final String sourceKey) throws SQLException {
        return getFirstInt(connection, "SELECT ID FROM ALERT.DEFINED_FIELDS WHERE DEFINED_FIELDS.SOURCE_KEY = '" + sourceKey + "' LIMIT 1;", "ID");
    }

    public static Integer getIdForConfigContext(final Connection connection, final String context) throws SQLException {
        return getFirstInt(connection, "SELECT ID FROM ALERT.CONFIG_CONTEXTS WHERE CONFIG_CONTEXTS.CONTEXT = '" + StringUtils.upperCase(context) + "' LIMIT 1;", "ID");
    }

    public static Integer getLatestIdForDescriptorConfig(final Connection connection) throws SQLException {
        return getFirstInt(connection, "SELECT ID FROM ALERT.DESCRIPTOR_CONFIGS ORDER BY DESCRIPTOR_CONFIGS.ID DESC LIMIT 1;", "ID");
    }

    public static void migrateIntValueIntoNewestConfig(final Connection connection, final String schemaName, final String tableName, final String columnName, final String fieldKey) throws SQLException {
        try {
            final Integer value = getFirstInt(connection, String.format("SELECT %s FROM %s.%s LIMIT 1;", columnName, schemaName, tableName), columnName);
            if (value != null) {
                migrateValueIntoNewestConfig(connection, fieldKey, value.toString());
            }
        } catch (final SQLException e) {
            if (!DOES_NOT_EXIST_MESSAGE.equals(e.getMessage())) {
                throw e;
            }
        }
    }

    public static void migrateBooleanValueIntoNewestConfig(final Connection connection, final String schemaName, final String tableName, final String columnName, final String fieldKey) throws SQLException {
        try {
            final Boolean value = getFirstBoolean(connection, String.format("SELECT %s FROM %s.%s LIMIT 1;", columnName, schemaName, tableName), columnName);
            if (value != null) {
                migrateValueIntoNewestConfig(connection, fieldKey, value.toString());
            }
        } catch (final SQLException e) {
            if (!DOES_NOT_EXIST_MESSAGE.equals(e.getMessage())) {
                throw e;
            }
        }
    }

    public static void migrateStringValueIntoNewestConfig(final Connection connection, final String schemaName, final String tableName, final String columnName, final String fieldKey) throws SQLException {
        try {
            final String value = getFirstString(connection, String.format("SELECT %s FROM %s.%s LIMIT 1;", columnName, schemaName, tableName), columnName);
            if (StringUtils.isNotBlank(value)) {
                migrateValueIntoNewestConfig(connection, fieldKey, String.format("'%s'", value));
            }
        } catch (final SQLException e) {
            if (!DOES_NOT_EXIST_MESSAGE.equals(e.getMessage())) {
                throw e;
            }
        }
    }

    private static void migrateValueIntoNewestConfig(final Connection connection, final String fieldKey, final String value) throws SQLException {
        try (final Statement insertIntoFieldValues = connection.createStatement()) {
            final Integer configId = getLatestIdForDescriptorConfig(connection);
            final Integer fieldId = getFieldIdForSourceKey(connection, fieldKey);
            insertIntoFieldValues.executeUpdate("INSERT INTO ALERT.FIELD_VALUES (CONFIG_ID, FIELD_ID, FIELD_VALUE) VALUES (" + configId + ", " + fieldId + ", " + value + ")");
        }
    }

    private static void ignoreUniquenessConstraintException(final SQLException e) throws SQLException {
        final String exceptionMessage = e.getMessage();
        if (!exceptionMessage.contains(UNIQUENESS_CONTRAINT_MESSAGE_SEGMENT)) {
            throw e;
        }
        // This is a duplicate key, but the relational tables still need to be updated.
    }

    private static Integer getFirstInt(final Connection connection, final String sql, final String columnName) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt(columnName);
            } else {
                throw new SQLException(DOES_NOT_EXIST_MESSAGE);
            }
        }
    }

    private static Boolean getFirstBoolean(final Connection connection, final String sql, final String columnName) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getBoolean(columnName);
            } else {
                throw new SQLException(DOES_NOT_EXIST_MESSAGE);
            }
        }
    }

    private static String getFirstString(final Connection connection, final String sql, final String columnName) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getString(columnName);
            } else {
                throw new SQLException(DOES_NOT_EXIST_MESSAGE);
            }
        }
    }
}
