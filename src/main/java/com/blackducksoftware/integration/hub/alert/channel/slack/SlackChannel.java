/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.channel.slack;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;
import com.google.gson.Gson;

public class SlackChannel extends DistributionChannel<SlackEvent, SlackConfigEntity> {

    @Autowired
    public SlackChannel(final Gson gson) {
        super(gson, SlackEvent.class);
    }

    @Override
    public void sendMessage(final SlackEvent event, final SlackConfigEntity config) {
        // TODO Auto-generated method stub

    }

    @Override
    public String testMessage(final SlackConfigEntity config) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void receiveMessage(final String message) {
        // TODO Auto-generated method stub

    }

}
