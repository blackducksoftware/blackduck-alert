package com.synopsys.integration.alert.workflow.processor.collector;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.workflow.processor.CollectorLookup;
import com.synopsys.integration.alert.common.workflow.processor.TopicCollector;

@Component
public class CollectorLookupService {

    private final Map<String, CollectorLookup> providerCollectorMap;

    @Autowired
    public CollectorLookupService(final List<ProviderDescriptor> providerDescriptorList) {
        providerCollectorMap = providerDescriptorList.stream().collect(Collectors.toMap(ProviderDescriptor::getName, (descriptor) -> descriptor.getProvider().getCollectorLookup()));
    }

    public Optional<TopicCollector> findCollector(final String providerName, final FormatType formatType) {
        if (providerCollectorMap.containsKey(providerName)) {
            final CollectorLookup providerCollectorLookup = providerCollectorMap.get(providerName);
            return providerCollectorLookup.findCollector(formatType);
        } else {
            return Optional.empty();
        }
    }
}
