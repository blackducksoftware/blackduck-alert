package com.synopsys.integration.alert.workflow.startup.component;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.workflow.startup.StartupManager;

@Component
@Order(3)
public class ConfigurationLogger extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(StartupManager.class);

    private final ProxyManager proxyManager;
    private final BlackDuckProperties blackDuckProperties;
    private final AlertProperties alertProperties;

    @Autowired
    public ConfigurationLogger(final ProxyManager proxyManager, final BlackDuckProperties blackDuckProperties, final AlertProperties alertProperties) {
        this.proxyManager = proxyManager;
        this.blackDuckProperties = blackDuckProperties;
        this.alertProperties = alertProperties;
    }

    @Override
    public void run() {
        final Optional<String> proxyHost = proxyManager.getProxyHost();
        final Optional<String> proxyPort = proxyManager.getProxyPort();
        final Optional<String> proxyUsername = proxyManager.getProxyUsername();
        final Optional<String> proxyPassword = proxyManager.getProxyPassword();

        final boolean authenticatedProxy = StringUtils.isNotBlank(proxyPassword.orElse(""));

        logger.info("----------------------------------------");
        logger.info("Alert Configuration: ");
        logger.info("Logging level:           {}", alertProperties.getLoggingLevel().orElse(""));
        logger.info("Alert Proxy Host:          {}", proxyHost.orElse(""));
        logger.info("Alert Proxy Port:          {}", proxyPort.orElse(""));
        logger.info("Alert Proxy Authenticated: {}", authenticatedProxy);
        logger.info("Alert Proxy User:          {}", proxyUsername.orElse(""));
        logger.info("");
        logger.info("BlackDuck URL:                 {}", blackDuckProperties.getBlackDuckUrl().orElse(""));
        logger.info("BlackDuck Webserver Host:                 {}", blackDuckProperties.getPublicBlackDuckWebserverHost().orElse(""));
        logger.info("BlackDuck Webserver Port:                 {}", blackDuckProperties.getPublicBlackDuckWebserverPort().orElse(""));
        final Optional<ConfigurationModel> optionalGlobalBlackDuckConfigEntity = blackDuckProperties.getBlackDuckConfig();
        optionalGlobalBlackDuckConfigEntity.ifPresent(configurationModel -> {
            final FieldAccessor fieldAccessor = new FieldAccessor(configurationModel.getCopyOfKeyToFieldMap());
            logger.info("BlackDuck API Token:           **********");
            logger.info("BlackDuck Timeout:             {}", fieldAccessor.getInteger(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT).orElse(BlackDuckProperties.DEFAULT_TIMEOUT));
        });
        logger.info("----------------------------------------");
    }
}
