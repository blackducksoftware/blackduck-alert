/**
 * issuetracker-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.issuetracker.service;

import java.util.List;

import com.google.gson.Gson;
import com.synopsys.integration.alert.issuetracker.config.IssueTrackerContext;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerRequest;
import com.synopsys.integration.alert.issuetracker.message.IssueTrackerResponse;
import com.synopsys.integration.exception.IntegrationException;

public abstract class IssueTrackerService<T extends IssueTrackerContext> {
    private Gson gson;

    public IssueTrackerService(Gson gson) {
        this.gson = gson;
    }

    /**
     * This method will send requests to an Issue Tracker to create, update, or resolve issues.
     * @param context  The object containing the configuration of the issue tracker server and the configuration of how to map and manage issues.
     * @param requests The list of requests to submit to the issue tracker.  Must be a list because the order requests are added matter.
     * @return A response object containing the aggregate status of sending the requests passed.
     * @throws IntegrationException
     */
    public abstract IssueTrackerResponse sendRequests(T context, List<IssueTrackerRequest> requests) throws IntegrationException;

    protected Gson getGson() {
        return gson;
    }
}
