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

public final class H2StoredProcedures {
    public static Integer getIdForRegisteredDescriptorName(final Connection connection, final String descriptorName) throws SQLException {
        return getFirstInt(connection, "SELECT id from alert.registered_descriptors WHERE registered_descriptors.name = '" + descriptorName + "' LIMIT 1;");
    }

    public static Integer getLatestFieldId(final Connection connection) throws SQLException {
        return getFirstInt(connection, "SELECT id FROM alert.defined_fields ORDER BY id DESC LIMIT 1;");
    }

    public static Integer getIdForConfigContext(final Connection connection, final String context) throws SQLException {
        return getFirstInt(connection, "SELECT id from alert.config_contexts WHERE config_contexts.context = '" + context + "' LIMIT 1;");
    }

    private static Integer getFirstInt(final Connection connection, final String sql) throws SQLException {
        try (final Statement statement = connection.createStatement()) {
            final ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                return resultSet.getInt("id");
            } else {
                throw new SQLException("Row does not exist");
            }
        }
    }
}
