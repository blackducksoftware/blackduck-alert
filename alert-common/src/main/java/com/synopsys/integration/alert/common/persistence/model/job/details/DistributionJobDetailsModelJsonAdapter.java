/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

public class DistributionJobDetailsModelJsonAdapter implements JsonSerializer<DistributionJobDetailsModel>, JsonDeserializer<DistributionJobDetailsModel> {
    @Override
    public JsonElement serialize(DistributionJobDetailsModel distributionJobDetailsModel, Type type, JsonSerializationContext context) {
        return context.serialize(distributionJobDetailsModel);
    }

    @Override
    public DistributionJobDetailsModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        MutableChannelKey mutableChannelKey = context.deserialize(jsonObject.getAsJsonObject("channelKey"), MutableChannelKey.class);
        Class<? extends DistributionJobDetailsModel> concreteClass = DistributionJobDetailsModel.getConcreteClass(mutableChannelKey.asChannelKey());
        if (null != concreteClass) {
            return context.deserialize(jsonObject, concreteClass);
        }
        throw new JsonParseException("Could not find a suitable class for deserialization");
    }

    private static final class MutableChannelKey {
        public String universalKey;
        public String displayName;

        public ChannelKey asChannelKey() {
            return new ChannelKey(universalKey, displayName);
        }

    }

}
