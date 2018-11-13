/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.workflow.filter.field;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.GsonJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.GsonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import com.synopsys.integration.alert.common.field.JsonField;
import com.synopsys.integration.alert.web.model.Config;

@Component
public class JsonExtractor {
    private final static Logger logger = LoggerFactory.getLogger(JsonExtractor.class);
    private final Gson gson;

    @Autowired
    public JsonExtractor(final Gson gson) {
        this.gson = gson;
        initializeJsonPath();
    }

    private void initializeJsonPath() {
        Configuration.setDefaults(new Configuration.Defaults() {

            private final JsonProvider jsonProvider = new GsonJsonProvider(gson);
            private final MappingProvider mappingProvider = new GsonMappingProvider(gson);

            @Override
            public JsonProvider jsonProvider() {
                return jsonProvider;
            }

            @Override
            public MappingProvider mappingProvider() {
                return mappingProvider;
            }

            @Override
            public Set<Option> options() {
                return EnumSet.of(Option.ALWAYS_RETURN_LIST);
            }
        });

    }

    public JsonFieldAccessor createJsonFieldAccessor(final List<JsonField<?>> fields, final String json) {
        final Map<JsonField, List<Object>> fieldToValuesMap = new HashMap<>();
        for (final JsonField field : fields) {
            final List<Object> values = getValuesFromJson(field.getTypeRef(), field.getJsonPath(), json);
            fieldToValuesMap.put(field, values);
        }
        return new JsonFieldAccessor(fieldToValuesMap);
    }

    public <T> List<T> getValuesFromConfig(final JsonField<T> field, final Config config) {
        final Optional<JsonPath> mapping = field.getConfigNameMapping();
        final List<T> values = new ArrayList<>();
        if (mapping.isPresent()) {
            values.addAll(getValuesFromObject(field.getTypeRef(), mapping.get(), config));
        }
        return values;
    }

    public <T> List<T> getValuesFromJson(final JsonField<T> field, final String json) {
        return getValuesFromJson(field.getTypeRef(), field.getJsonPath(), json);
    }

    private <T> List<T> getValuesFromJson(final TypeRef<?> typeRef, final JsonPath jsonPath, final String json) {
        final List values = new ArrayList<>();
        try {
            final Object obj = JsonPath.parse(json).read(jsonPath, typeRef);
            if (Collection.class.isAssignableFrom(obj.getClass())) {
                values.addAll((Collection) obj);
            } else {
                values.add(obj);
            }
        } catch (final PathNotFoundException e) {
            logger.debug(String.format("Could not find the path: %s. For: %s", jsonPath.getPath(), json), e);
        }
        return values;
    }

    private <T> List<T> getValuesFromObject(final TypeRef<?> typeRef, final JsonPath jsonPath, final Object json) {
        return getValuesFromJson(typeRef, jsonPath, gson.toJson(json));
    }
}
