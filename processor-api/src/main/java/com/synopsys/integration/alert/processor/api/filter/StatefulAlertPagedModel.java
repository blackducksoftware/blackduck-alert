package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.function.BiFunction;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class StatefulAlertPagedModel<T extends AlertSerializableModel> {
    private final int pageSize;
    private int pageNumber;
    private final List<T> currentModels;
    private final BiFunction<Integer, Integer, AlertPagedModel<T>> retrievePage;

    public StatefulAlertPagedModel(int pageNumber, int pageSize, List<T> currentModels, BiFunction<Integer, Integer, AlertPagedModel<T>> retrievePage) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.currentModels = currentModels;
        this.retrievePage = retrievePage;
    }

    public List<T> getCurrentModels() {
        return currentModels;
    }

    public StatefulAlertPagedModel<T> retrieveNextPage() {
        AlertPagedModel<T> nextPage = retrievePage.apply(pageNumber + 1, pageSize);
        return new StatefulAlertPagedModel<>(pageNumber + 1, pageSize, nextPage.getModels(), retrievePage);
    }

    public boolean hasNextPage() {
        return currentModels.size() < pageSize;
    }
}
