/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.slack.descriptor;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.action.CustomEndpointManager;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.web.controller.ResponseFactory;

@Component
public class SlackCustomEndpoint {
    private Gson gson;
    private ResponseFactory responseFactory;

    @Autowired
    public SlackCustomEndpoint(final CustomEndpointManager customEndpointManager, final Gson gson, final ResponseFactory responseFactory) throws AlertException {
        this.gson = gson;
        this.responseFactory = responseFactory;

        customEndpointManager.registerFunction(SlackDescriptor.KEY_CHANNEL_USERNAME, this::retrieveTableData);
    }

    // Return a list of objects with the names of keys as the variable names
    public ResponseEntity<String> retrieveTableData(Map<String, FieldValueModel> fieldValues) {
        List<ColumnData> columnData = new LinkedList<>();
        columnData.add(new ColumnData("z", "This should be last"));
        columnData.add(new ColumnData("ABC", "abc's"));
        columnData.add(new ColumnData("Should be sorted", "Columns should be sorted"));
        return responseFactory.createOkContentResponse(gson.toJson(columnData));
    }

    class ColumnData {
        private String columnHeader;
        private String columnDescription;

        public ColumnData(final String columnHeader, final String columnDescription) {
            this.columnHeader = columnHeader;
            this.columnDescription = columnDescription;
        }
    }

}
