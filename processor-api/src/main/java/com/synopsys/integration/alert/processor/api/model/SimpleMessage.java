/**
 * provider
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
package com.synopsys.integration.alert.processor.api.model;

import java.util.List;

import com.synopsys.integration.alert.common.message.model.LinkableItem;

public class SimpleMessage extends ProviderMessage<SimpleMessage> {
    private final String subject;
    private final String description;
    private final List<LinkableItem> details;

    public SimpleMessage(LinkableItem provider, String subject, String description, List<LinkableItem> details) {
        super(provider);
        this.subject = subject;
        this.description = description;
        this.details = details;
    }

    public String getSubject() {
        return subject;
    }

    public String getDescription() {
        return description;
    }

    public List<LinkableItem> getDetails() {
        return details;
    }

    @Override
    public List<SimpleMessage> combine(SimpleMessage otherMessage) {
        return List.of(this, otherMessage);
    }

}
