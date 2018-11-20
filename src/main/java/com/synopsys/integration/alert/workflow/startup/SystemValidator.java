package com.synopsys.integration.alert.workflow.startup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.blackduck.service.model.HubServerVerifier;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;

@Component
public class SystemValidator {
    private static final Logger logger = LoggerFactory.getLogger(SystemValidator.class);
    private final AlertProperties alertProperties;
    private final BlackDuckProperties blackDuckProperties;
    private final EncryptionUtility encryptionUtility;
    private final SystemStatusUtility systemStatusUtility;
    private final SystemMessageUtility systemMessageUtility;

    @Autowired
    public SystemValidator(final AlertProperties alertProperties, final BlackDuckProperties blackDuckProperties, final EncryptionUtility encryptionUtility, final SystemStatusUtility systemStatusUtility,
        final SystemMessageUtility systemMessageUtility) {
        this.alertProperties = alertProperties;
        this.blackDuckProperties = blackDuckProperties;
        this.encryptionUtility = encryptionUtility;
        this.systemStatusUtility = systemStatusUtility;
        this.systemMessageUtility = systemMessageUtility;
    }

    public void validate() {
        boolean valid = true;
        valid = valid && validateEncryptionProperties();
        valid = valid && validateProviders();
        systemStatusUtility.setSystemInitialized(valid);

    }

    public boolean validateEncryptionProperties() {
        final boolean valid;
        if (encryptionUtility.isInitialized()) {
            logger.info("Encryption utilities: Initialized");
            valid = true;
            systemMessageUtility.removeSystemMessagesByType(SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        } else {
            logger.error("Encryption utilities: Not Initialized");
            final List<String> errors = encryptionUtility.checkForErrors();
            errors.forEach(errorMessage -> systemMessageUtility.addSystemMessage(errorMessage, SystemMessageSeverity.ERROR, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR));
            valid = false;
        }
        return valid;
    }

    public boolean validateProviders() {
        final boolean valid;
        logger.info("Validating configured providers: ");
        logger.info("----------------------------------------");
        valid = validateBlackDuckProvider();
        logger.info("----------------------------------------");
        return valid;
    }

    // TODO add this validation to provider descriptors so we can run this when it's defined
    public boolean validateBlackDuckProvider() {
        logger.info("Validating BlackDuck Provider...");
        boolean valid = true;
        try {
            final HubServerVerifier verifier = new HubServerVerifier();
            final ProxyInfoBuilder proxyBuilder = createProxyInfoBuilder();
            final ProxyInfo proxyInfo = proxyBuilder.build();
            final Optional<String> blackDuckUrlOptional = blackDuckProperties.getBlackDuckUrl();
            if (!blackDuckUrlOptional.isPresent()) {
                logger.error("  -> BlackDuck Provider Invalid; cause: Black Duck URL missing...");
                systemMessageUtility.addSystemMessage("BlackDuck Provider invalid: URL missing", SystemMessageSeverity.ERROR, SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
                valid = false;
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
                verifier.verifyIsHubServer(blackDuckUrl, proxyInfo, trustCertificate, timeout);
                logger.info("  -> BlackDuck Provider Valid!");
            }
        } catch (final MalformedURLException | IntegrationException ex) {
            logger.error("  -> BlackDuck Provider Invalid; cause: {}", ex.getMessage());
            logger.debug("  -> BlackDuck Provider Stack Trace: ", ex);
            systemMessageUtility.addSystemMessage("BlackDuck Provider invalid: " + ex.getMessage(), SystemMessageSeverity.ERROR, SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
            valid = false;
        }

        if (valid) {
            systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_CONNECTIVITY);
            systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_URL_MISSING);
            systemMessageUtility.removeSystemMessagesByType(SystemMessageType.BLACKDUCK_PROVIDER_LOCALHOST);
        }

        return valid;
    }

    private ProxyInfoBuilder createProxyInfoBuilder() {
        final ProxyInfoBuilder proxyBuilder = new ProxyInfoBuilder();
        final Optional<String> alertProxyHost = alertProperties.getAlertProxyHost();
        final Optional<String> alertProxyPort = alertProperties.getAlertProxyPort();
        final Optional<String> alertProxyUsername = alertProperties.getAlertProxyUsername();
        final Optional<String> alertProxyPassword = alertProperties.getAlertProxyPassword();
        if (alertProxyHost.isPresent()) {
            proxyBuilder.setHost(alertProxyHost.get());
        }
        if (alertProxyPort.isPresent()) {
            proxyBuilder.setPort(NumberUtils.toInt(alertProxyPort.get()));
        }
        if (alertProxyUsername.isPresent()) {
            proxyBuilder.setUsername(alertProxyUsername.get());
        }
        if (alertProxyPassword.isPresent()) {
            proxyBuilder.setPassword(alertProxyPassword.get());
        }
        return proxyBuilder;
    }
}
