package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;

// TODO update this when we have completed models
public interface NotificationFilter {
    <T> List<T> filter(List<Object> notifications);

}
