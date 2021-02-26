/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.workflow.combiner;

import java.util.List;

import com.synopsys.integration.alert.common.message.model.ProviderMessageContent;

public interface MessageCombiner {
    List<ProviderMessageContent> combine(List<ProviderMessageContent> messages);

}
