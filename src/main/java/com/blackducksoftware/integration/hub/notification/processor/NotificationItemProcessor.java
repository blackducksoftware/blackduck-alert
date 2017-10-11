package com.blackducksoftware.integration.hub.notification.processor;

import java.util.ArrayList;
import java.util.Collection;

import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.vulnerability.VulnerabilityRequestService;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyOverrideContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationClearedContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.PolicyViolationContentItem;
import com.blackducksoftware.integration.hub.dataservice.notification.model.VulnerabilityContentItem;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.notification.event.DBStoreEvent;
import com.blackducksoftware.integration.hub.notification.processor.MapProcessorCache;
import com.blackducksoftware.integration.hub.notification.processor.NotificationProcessor;
import com.blackducksoftware.integration.hub.notification.processor.event.NotificationEvent;
import com.blackducksoftware.integration.hub.service.HubResponseService;

public class NotificationItemProcessor extends NotificationProcessor<DBStoreEvent> {

    public NotificationItemProcessor(final HubResponseService hubResponseService, final VulnerabilityRequestService vulnerabilityRequestService, final MetaService metaService) {
        final MapProcessorCache policyCache = new MapProcessorCache();
        final VulnerabilityCache vulnerabilityCache = new VulnerabilityCache(hubResponseService, vulnerabilityRequestService, metaService);
        getCacheList().add(policyCache);
        getCacheList().add(vulnerabilityCache);
        getProcessorMap().put(PolicyViolationContentItem.class, new PolicyViolationProcessor(policyCache, metaService));
        getProcessorMap().put(PolicyViolationClearedContentItem.class, new PolicyViolationClearedProcessor(policyCache, metaService));
        getProcessorMap().put(PolicyOverrideContentItem.class, new PolicyOverrideProcessor(policyCache, metaService));
        getProcessorMap().put(VulnerabilityContentItem.class, new VulnerabilityProcessor(vulnerabilityCache, metaService));
    }

    @Override
    public DBStoreEvent processEvents(final Collection<NotificationEvent> eventList) throws HubIntegrationException {
        final DBStoreEvent dbStoreEvent = new DBStoreEvent(new ArrayList<>(eventList));
        return dbStoreEvent;
    }
}
