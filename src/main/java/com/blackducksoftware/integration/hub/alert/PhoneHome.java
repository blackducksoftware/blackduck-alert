package com.blackducksoftware.integration.hub.alert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;

@Component
public class PhoneHome {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GlobalProperties globalProperties;
    private final CommonDistributionRepositoryWrapper commonDistributionRepositoryWrapper;

    @Autowired
    public PhoneHome(final GlobalProperties globalProperties, final CommonDistributionRepositoryWrapper commonDistributionRepositoryWrapper) {
        this.globalProperties = globalProperties;
        this.commonDistributionRepositoryWrapper = commonDistributionRepositoryWrapper;
    }

    public PhoneHomeService createPhoneHomeService() {
        final HubServicesFactory hubServicesFactory = globalProperties.createHubServicesFactoryAndLogErrors(logger);
        final PhoneHomeService phoneHomeService = hubServicesFactory.createPhoneHomeService();
        return phoneHomeService;
    }

    public PhoneHomeRequestBody.Builder createPhoneHomeBuilder(final PhoneHomeService phoneHomeService) {
        final String productVersion = globalProperties.getProductVersion();
        if (GlobalProperties.PRODUCT_VERSION_UNKNOWN.equals(productVersion)) {
            return null;
        }

        final PhoneHomeRequestBody.Builder phoneHomeRequestBodyBuilder = phoneHomeService.createInitialPhoneHomeRequestBodyBuilder("blackduck-alert", productVersion);
        return phoneHomeRequestBodyBuilder;
    }

    public PhoneHomeRequestBody.Builder addChannelMetaData(final PhoneHomeRequestBody.Builder phoneHomeRequestBody) {
        final Map<String, Integer> createdSupportedChannels = getChannelMetaData();
        for (final String supportedChannel : createdSupportedChannels.keySet()) {
            final Integer count = createdSupportedChannels.get(supportedChannel);
            final String supportedChannelkey = "channel." + supportedChannel;
            phoneHomeRequestBody.addToMetaData(supportedChannelkey, count.toString());
        }

        return phoneHomeRequestBody;
    }

    private Map<String, Integer> getChannelMetaData() {
        final List<CommonDistributionConfigEntity> commonConfigList = commonDistributionRepositoryWrapper.findAll();
        final Map<String, Integer> createdSupportedChannels = new HashMap<>();
        for (final CommonDistributionConfigEntity commonConfigEntity : commonConfigList) {
            final String supportedChannel = commonConfigEntity.getDistributionType();
            if (createdSupportedChannels.containsKey(supportedChannel)) {
                final int count = createdSupportedChannels.get(supportedChannel);
                createdSupportedChannels.put(supportedChannel, count + 1);
            } else {
                createdSupportedChannels.put(supportedChannel, 1);
            }
        }

        return createdSupportedChannels;
    }

}
