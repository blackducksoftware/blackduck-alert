/**
 * processor-api
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
package com.synopsys.integration.alert.processor.api.extract.model;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections4.ListUtils;

public interface CombinableModel<T> {
    List<T> combine(T otherModel);

    static <T extends CombinableModel<T>> List<T> combine(List<T> lhs, List<T> rhs) {
        List<T> unified = ListUtils.union(lhs, rhs);
        return combine(unified);
    }

    static <T extends CombinableModel<T>> List<T> combine(List<T> models) {
        LinkedList<T> copyOfModels = new LinkedList<>(models);
        int curIndex = 0;
        int combineWithIndex;
        while (curIndex < copyOfModels.size()) {
            T curElement = copyOfModels.get(curIndex);
            combineWithIndex = curIndex + 1;
            while (combineWithIndex < copyOfModels.size()) {
                T combineWithElement = copyOfModels.get(combineWithIndex);
                List<T> combinedElements = curElement.combine(combineWithElement);

                int combinedSize = combinedElements.size();
                if (combinedSize == 0) {
                    copyOfModels.remove(curElement);
                    copyOfModels.remove(combineWithElement);
                    break;
                } else if (combinedSize == 1) {
                    copyOfModels.remove(curElement);
                    copyOfModels.remove(combineWithElement);
                    copyOfModels.add(curIndex, combinedElements.get(0));
                    break;
                } else {
                    combineWithIndex++;
                }
            }

            if (combineWithIndex >= copyOfModels.size()) {
                curIndex++;
            }
        }
        return copyOfModels;
    }

}
