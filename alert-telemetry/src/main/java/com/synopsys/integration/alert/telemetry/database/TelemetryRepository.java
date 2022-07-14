/*
 * alert-telemetry
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.telemetry.database;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.telemetry.model.NotificationMappingTelemetryModel;
import com.synopsys.integration.alert.telemetry.model.NotificationProcessingTelemetryModel;

//TODO: This class is set up as a mockup of what the future database tables should look like.
//  When implementing in the future, use an interface extending JpaRepository<TelemetryEntity, UUID>
@Component
public class TelemetryRepository {
    Map<UUID, NotificationMappingTelemetryModel> jobMapping = new HashMap<>();
    Map<UUID, NotificationProcessingTelemetryModel> processingMapping = new HashMap<>();
    Map<UUID, String> distributionEvent = new HashMap<>();

    public NotificationMappingTelemetryModel getOneMapping(UUID uuid) {
        return jobMapping.get(uuid);
    }

    public NotificationMappingTelemetryModel saveMapping(UUID uuid, NotificationMappingTelemetryModel notificationMappingTelemetryModel) {
        return jobMapping.put(uuid, notificationMappingTelemetryModel);
    }

    public NotificationProcessingTelemetryModel getOneProcessing(UUID uuid) {
        return processingMapping.get(uuid);
    }

    public NotificationProcessingTelemetryModel saveProcessing(UUID uuid, NotificationProcessingTelemetryModel notificationProcessingTelemetryModel) {
        return processingMapping.put(uuid, notificationProcessingTelemetryModel);
    }
}
