package com.synopsys.integration.alert.provider.blackduck;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.provider.ProviderValidator;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.blackduck.configuration.BlackDuckServerConfig;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckValidator extends ProviderValidator {
    private static final Logger logger = LoggerFactory.getLogger(BlackDuckValidator.class);

    private final AlertProperties alertProperties;
    private final BlackDuckProperties blackDuckProperties;
    private final SystemMessageUtility systemMessageUtility;

    @Autowired
    public BlackDuckValidator(final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final SystemMessageUtility systemMessageUtility) {
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.systemMessageUtility = systemMessageUtility;
    }

    @Override
    public boolean validate() {
        logger.info("Validating BlackDuck Provider...");
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
        try {

            final Optional<String> blackDuckUrlOptional = blackDuckProperties.getBlackDuckUrl();
            if (blackDuckUrlOptional.isEmpty()) {
                logger.error("  -> BlackDuck Provider Invalid; cause: Black Duck URL missing...");
                final String errorMessage = "BlackDuck Provider invalid: URL missing";
                systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
            } else {
                final String blackDuckUrlString = blackDuckUrlOptional.get();
                final Boolean trustCertificate = BooleanUtils.toBoolean(alertProperties.getAlertTrustCertificate().orElse(false));
                final Integer timeout = blackDuckProperties.getBlackDuckTimeout();
                logger.debug("  -> BlackDuck Provider URL found validating: {}", blackDuckUrlString);
                logger.debug("  -> BlackDuck Provider Trust Cert: {}", trustCertificate);
                logger.debug("  -> BlackDuck Provider Timeout: {}", timeout);
                final URL blackDuckUrl = new URL(blackDuckUrlString);
                if ("localhost".equals(blackDuckUrl.getHost())) {
                    logger.warn("  -> BlackDuck Provider Using localhost...");
                    final String blackDuckWebServerHost = blackDuckProperties.getPublicBlackDuckWebserverHost().orElse("");
                    logger.warn("  -> BlackDuck Provider Using localhost because PUBLIC_BLACKDUCK_WEBSERVER_HOST environment variable is set to {}", blackDuckWebServerHost);
                    systemMessageUtility.addSystemMessage("BlackDuck Provider Using localhost", SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
                }
                final IntLogger intLogger = new Slf4jIntLogger(logger);
                final Optional<BlackDuckServerConfig> blackDuckServerConfig = blackDuckProperties.createBlackDuckServerConfig(intLogger);
                if (blackDuckServerConfig.isPresent()) {
                    final Boolean canConnect = blackDuckServerConfig.get().canConnect(intLogger);
                    if (canConnect) {
                        logger.info("  -> BlackDuck Provider Valid!");
                    } else {
                        final String message = "Can not connect to the BlackDuck server with the current configuration.";
                        connectivityWarning(systemMessageUtility, message);
                    }
                } else {
                    final String message = "The BlackDuck configuration is not valid.";
                    connectivityWarning(systemMessageUtility, message);
                }
            }
        } catch (final MalformedURLException | IntegrationException | AlertRuntimeException ex) {
            logger.error("  -> BlackDuck Provider Invalid; cause: {}", ex.getMessage());
            logger.debug("  -> BlackDuck Provider Stack Trace: ", ex);
            systemMessageUtility.addSystemMessage("BlackDuck Provider invalid: " + ex.getMessage(), SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
        }
        return true;
    }

    private void connectivityWarning(final SystemMessageUtility systemMessageUtility, final String message) {
        logger.warn(message);
        systemMessageUtility.addSystemMessage(message, SystemMessageSeverity.WARNING, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
    }
}
