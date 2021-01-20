package com.synopsys.integration.alert.processor.api.filter.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public final class NotificationFilterMapModel extends AlertSerializableModel {
    private final Map<ProcessingType, List<NotificationFilterJobModel>> jobFilterMapping;

    public NotificationFilterMapModel(Map<ProcessingType, List<NotificationFilterJobModel>> jobFilterMapping) {
        this.jobFilterMapping = jobFilterMapping;
    }

    public List<NotificationFilterJobModel> getJobModels(ProcessingType processingType) {
        return jobFilterMapping.get(processingType);
    }

    public Map<ProcessingType, List<NotificationFilterJobModel>> getJobFilterMapping() {
        return jobFilterMapping;
    }

    public Set<ProcessingType> getProcessingTypes() {
        return jobFilterMapping.keySet();
    }

}
