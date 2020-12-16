/**
 * alert-common
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
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DistributionJobDetailsModelJsonAdapter implements JsonSerializer<DistributionJobDetailsModel>, JsonDeserializer<DistributionJobDetailsModel> {
    @Override
    public JsonElement serialize(DistributionJobDetailsModel distributionJobDetailsModel, Type type, JsonSerializationContext context) {
        if (distributionJobDetailsModel.isAzureBoardsDetails()) {
            return context.serialize(distributionJobDetailsModel.getAsAzureBoardsJobDetails());
        } else if (distributionJobDetailsModel.isEmailDetails()) {
            return context.serialize(distributionJobDetailsModel.getAsEmailJobDetails());
        } else if (distributionJobDetailsModel.isJiraCloudDetails()) {
            return context.serialize(distributionJobDetailsModel.getAsJiraCouldJobDetails());
        } else if (distributionJobDetailsModel.isJiraServerDetails()) {
            return context.serialize(distributionJobDetailsModel.getAsJiraServerJobDetails());
        } else if (distributionJobDetailsModel.isMSTeamsDetails()) {
            return context.serialize(distributionJobDetailsModel.getAsMSTeamsJobDetails());
        } else if (distributionJobDetailsModel.isSlackDetails()) {
            return context.serialize(distributionJobDetailsModel.getAsSlackJobDetails());
        } else {
            return context.serialize(distributionJobDetailsModel);
        }
    }

    @Override
    public DistributionJobDetailsModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonPrimitive channelDescriptorName = jsonObject.getAsJsonPrimitive("channelDescriptorName");
        DistributionJobDetailsModel distributionJobDetailsModel = new DistributionJobDetailsModel(channelDescriptorName.getAsString()) {};
        if (distributionJobDetailsModel.isAzureBoardsDetails()) {
            return context.deserialize(jsonObject, AzureBoardsJobDetailsModel.class);
        } else if (distributionJobDetailsModel.isEmailDetails()) {
            return context.deserialize(jsonObject, EmailJobDetailsModel.class);
        } else if (distributionJobDetailsModel.isJiraCloudDetails()) {
            return context.deserialize(jsonObject, JiraCloudJobDetailsModel.class);
        } else if (distributionJobDetailsModel.isJiraServerDetails()) {
            return context.deserialize(jsonObject, JiraServerJobDetailsModel.class);
        } else if (distributionJobDetailsModel.isMSTeamsDetails()) {
            return context.deserialize(jsonObject, MSTeamsJobDetailsModel.class);
        } else if (distributionJobDetailsModel.isSlackDetails()) {
            return context.deserialize(jsonObject, SlackJobDetailsModel.class);
        } else {
            throw new JsonParseException("Could not determine an appropriate sub-class for " + type);
        }
    }

}
