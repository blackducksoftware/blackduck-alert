package com.synopsys.integration.alert.processor.api.filter;

import com.synopsys.integration.alert.common.rest.model.AlertPagedDetails;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.util.Stringable;

public interface NextPageRetriever<T extends Stringable> {

    AlertPagedDetails<T> retrieveNextPage(int currentOffset, int currentLimit) throws IntegrationException;
}
