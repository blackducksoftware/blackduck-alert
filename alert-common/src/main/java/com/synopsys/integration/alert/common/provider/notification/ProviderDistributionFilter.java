/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.provider.notification;

import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.rest.model.AlertNotificationModel;
import com.synopsys.integration.alert.common.workflow.cache.NotificationDeserializationCache;

public interface ProviderDistributionFilter {
    boolean doesNotificationApplyToConfiguration(AlertNotificationModel notification, DistributionJobModel configurationJobModel);

    NotificationDeserializationCache getCache();

}
