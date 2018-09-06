package com.synopsys.integration.alert.common.workflow.processor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.enumeration.FormatType;

public class CollectorLookup {
    private final Map<FormatType, TopicCollector> formatCollectorMap;

    public CollectorLookup(final List<TopicCollector> topicCollectorList) {
        formatCollectorMap = topicCollectorList.stream().collect(Collectors.toMap(TopicCollector::getFormat, Function.identity()));
    }

    public Optional<TopicCollector> findCollector(final FormatType formatType) {
        if (formatCollectorMap.containsKey(formatType)) {
            return Optional.of(formatCollectorMap.get(formatType));
        } else {
            return Optional.empty();
        }
    }
}
