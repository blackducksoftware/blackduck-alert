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

    public static void defineField(final Connection connection, final String fieldKey, final Boolean sensitive, final String descriptorName, final String context) throws SQLException {
        try (final Statement insertIntoDefinedFields = connection.createStatement()) {
            insertIntoDefinedFields.executeUpdate("INSERT INTO ALERT.DEFINED_FIELDS (SOURCE_KEY, SENSITIVE) VALUES ('" + fieldKey + "', " + sensitive + ");");
        } catch (final SQLException e) {
            ignoreUniquenessConstraintException(e);
        }

        final Integer fieldId = getFirstInt(connection, "SELECT ID FROM ALERT.DEFINED_FIELDS WHERE DEFINED_FIELDS.SOURCE_KEY = '" + fieldKey + "' LIMIT 1;");
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
        return getFirstInt(connection, "SELECT ID FROM ALERT.REGISTERED_DESCRIPTORS WHERE REGISTERED_DESCRIPTORS.NAME = '" + StringUtils.lowerCase(descriptorName) + "' LIMIT 1;");
    }

    public static Integer getIdForDescriptorType(final Connection connection, final String type) throws SQLException {
        return getFirstInt(connection, "SELECT ID FROM ALERT.DESCRIPTOR_TYPES WHERE DESCRIPTOR_TYPES.TYPE = '" + StringUtils.upperCase(type) + "' LIMIT 1;");
    }

    public static Integer getLatestFieldId(final Connection connection) throws SQLException {
        return getFirstInt(connection, "SELECT ID FROM ALERT.DEFINED_FIELDS ORDER BY ID DESC LIMIT 1;");
    }

    public static Integer getIdForConfigContext(final Connection connection, final String context) throws SQLException {
        return getFirstInt(connection, "SELECT ID FROM ALERT.CONFIG_CONTEXTS WHERE CONFIG_CONTEXTS.CONTEXT = '" + StringUtils.upperCase(context) + "' LIMIT 1;");
    }

    private static void ignoreUniquenessConstraintException(final SQLException e) throws SQLException {
        final String exceptionMessage = e.getMessage();
        if (!exceptionMessage.contains(UNIQUENESS_CONTRAINT_MESSAGE_SEGMENT)) {
            throw e;
        }
        // This is a duplicate key, but the relational tables still need to be updated.
    }

    private static Integer getFirstInt(final Connection connection, final String sql) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt("ID");
            } else {
                throw new SQLException("Row does not exist");
            }
        }
    }
}
