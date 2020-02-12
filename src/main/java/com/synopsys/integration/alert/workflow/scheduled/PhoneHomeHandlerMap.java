package com.synopsys.integration.alert.workflow.scheduled;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.provider.Provider;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.provider.ProviderPhoneHomeHandler;
import com.synopsys.integration.phonehome.PhoneHomeRequestBody;

@Component
public class PhoneHomeHandlerMap {
    private List<ProviderPhoneHomeHandler> handlers;
    private Map<ProviderKey, ProviderPhoneHomeHandler> handlerMap;

    @Autowired
    public PhoneHomeHandlerMap(List<ProviderPhoneHomeHandler> handlers) {
        this.handlers = handlers;
        handlerMap = this.handlers.stream()
                         .collect(Collectors.toMap(ProviderPhoneHomeHandler::getProviderKey, Function.identity()));
    }

    public PhoneHomeRequestBody.Builder populatePhoneHomeData(Provider provider, ConfigurationModel configurationModel, PhoneHomeRequestBody.Builder phoneHomeBuilder) {
        if (!handlerMap.containsKey(provider.getKey())) {
            return phoneHomeBuilder;
        }
        ProviderPhoneHomeHandler handler = handlerMap.get(provider.getKey());
        return handler.populatePhoneHomeData(configurationModel, phoneHomeBuilder);
    }
}
