/*
 * api-provider
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.provider;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.phonehome.request.PhoneHomeRequestBodyBuilder;
import com.synopsys.integration.util.NameVersion;

public interface ProviderPhoneHomeHandler {
    ProviderKey getProviderKey();

    PhoneHomeRequestBodyBuilder populatePhoneHomeData(ConfigurationModel configurationModel, NameVersion alertArtifactInfo);

}
