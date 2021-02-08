/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopys.integration.alert.channel.api.issue.model;

import java.io.Serializable;
import java.util.List;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class IssueTransitionModel<T extends Serializable> extends AlertSerializableModel {
    private final T issueId;
    private final String transitionKey;
    private final List<String> postTransitionComments;

    public IssueTransitionModel(T issueId, String transitionKey, List<String> postTransitionComments) {
        this.issueId = issueId;
        this.transitionKey = transitionKey;
        this.postTransitionComments = postTransitionComments;
    }

    public T getIssueId() {
        return issueId;
    }

    public String getTransitionKey() {
        return transitionKey;
    }

    public List<String> getPostTransitionComments() {
        return postTransitionComments;
    }

}
