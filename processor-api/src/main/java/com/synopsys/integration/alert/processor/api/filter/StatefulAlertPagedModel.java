package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.function.BiFunction;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class StatefulAlertPagedModel<T extends AlertSerializableModel> {
    private final int pageSize;
    private final int pageNumber;
    private final List<T> currentModels;
    private final BiFunction<Integer, Integer, AlertPagedModel<T>> retrievePage;

    public StatefulAlertPagedModel(int pageSize, int pageNumber, List<T> currentModels, BiFunction<Integer, Integer, AlertPagedModel<T>> retrievePage) {
        this.pageSize = pageSize;
        this.pageNumber = pageNumber;
        this.currentModels = currentModels;
        this.retrievePage = retrievePage;
    }

    public List<T> getCurrentModels() {
        return currentModels;
    }

    public StatefulAlertPagedModel<T> retrieveNextPage() {
        //TODO: Confirm the parameter order
        AlertPagedModel<T> nextPage = retrievePage.apply(pageSize, pageNumber);
        return new StatefulAlertPagedModel<>(pageSize, pageNumber + 1, nextPage.getModels(), retrievePage);
    }

    public boolean hasNextPage() {
        return currentModels.size() < pageSize;
    }
}
