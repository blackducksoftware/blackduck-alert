package com.synopsys.integration.alert.common.provider;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.phonehome.PhoneHomeRequestBody;

public interface ProviderPhoneHomeHandler {
    ProviderKey getProviderKey();

    PhoneHomeRequestBody.Builder populatePhoneHomeData(ConfigurationModel configurationModel, PhoneHomeRequestBody.Builder phoneHomeBuilder);
}
