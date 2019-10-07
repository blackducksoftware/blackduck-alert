/**
 * alert-common
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
package com.synopsys.integration.alert.common.util;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AlertUtils {

    public static <K, V> Map<K, V> convertToMapWithCopiedValue(Collection<V> valueCollection, Function<V, K> keyConverter) {
        return valueCollection.stream().collect(Collectors.toMap(keyConverter::apply, Function.identity()));
    }

    public static <K, V> Map<K, V> convertToMapWithCopiedKey(Collection<K> valueCollection, Function<K, V> valueConverter) {
        return valueCollection.stream().collect(Collectors.toMap(Function.identity(), valueConverter::apply));
    }

    public static Date createCurrentDateTimestamp() {
        final ZonedDateTime zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        return Date.from(zonedDateTime.toInstant());
    }

}
