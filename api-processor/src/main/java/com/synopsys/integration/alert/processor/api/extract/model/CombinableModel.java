/*
 * api-processor
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
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
