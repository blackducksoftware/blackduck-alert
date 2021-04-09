package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.function.BiFunction;

import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class StatefulAlertPagedModel<T extends AlertSerializableModel> {
    private final int totalPages;
    private final int pageNumber;
    private final int pageSize;
    private final List<T> currentModels;
    private final BiFunction<Integer, Integer, AlertPagedModel<T>> retrievePage;

    public static <T extends AlertSerializableModel> StatefulAlertPagedModel<T> empty() {
        return new StatefulAlertPagedModel<>(0, 0, 0, List.of(), (x, y) -> AlertPagedModel.empty());
    }

    public StatefulAlertPagedModel(int totalPages, int pageNumber, int pageSize, List<T> currentModels, BiFunction<Integer, Integer, AlertPagedModel<T>> retrievePage) {
        this.totalPages = totalPages;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.currentModels = currentModels;
        this.retrievePage = retrievePage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<T> getCurrentModels() {
        return currentModels;
    }

    public boolean hasModels() {
        return !currentModels.isEmpty();
    }

    public StatefulAlertPagedModel<T> retrieveNextPage() {
        AlertPagedModel<T> nextPage = retrievePage.apply(pageNumber + 1, pageSize);
        return new StatefulAlertPagedModel<>(totalPages, pageNumber + 1, pageSize, nextPage.getModels(), retrievePage);
    }

    public boolean hasNextPage() {
        return pageNumber < totalPages - 1;
    }
}
