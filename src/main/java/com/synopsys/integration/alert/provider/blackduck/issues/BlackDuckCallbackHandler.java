package com.synopsys.integration.alert.provider.blackduck.issues;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.event.ProviderCallbackEvent;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.workflow.ProviderCallbackHandler;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.issuetracker.common.IssueOperation;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Component
public class BlackDuckCallbackHandler extends ProviderCallbackHandler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    public BlackDuckCallbackHandler(BlackDuckProvider blackDuckProvider, ConfigurationAccessor configurationAccessor, Gson gson) {
        super(blackDuckProvider, configurationAccessor, gson);
    }

    @Override
    protected void performProviderCallback(ProviderCallbackEvent event, StatefulProvider statefulProvider) throws IntegrationException {
        BlackDuckProperties blackDuckProperties = (BlackDuckProperties) statefulProvider.getProperties();
        IntLogger intLogger = new Slf4jIntLogger(logger);
        Optional<BlackDuckServicesFactory> optionalBlackDuckServicesFactory = blackDuckProperties.createBlackDuckHttpClient(intLogger)
                                                                                  .map(httpClient -> blackDuckProperties.createBlackDuckServicesFactory(httpClient, intLogger));
        if (optionalBlackDuckServicesFactory.isPresent()) {
            BlackDuckServicesFactory blackDuckServicesFactory = optionalBlackDuckServicesFactory.get();
            BlackDuckProviderIssueHandler blackDuckProviderIssueHandler = new BlackDuckProviderIssueHandler(blackDuckServicesFactory.createBlackDuckService());

            BlackDuckProviderIssueModel issueModel = createBlackDuckIssueModel(event);
            blackDuckProviderIssueHandler.createOrUpdateBlackDuckIssue(event.getCallbackUrl(), issueModel);
        } else {
            logger.error("Cannot instantiate the BlackDuck services from a seemingly valid properties object. Config: id='{}', name='{}'", statefulProvider.getConfigId(), statefulProvider.getConfigName());
        }
    }

    private BlackDuckProviderIssueModel createBlackDuckIssueModel(ProviderCallbackEvent event) {
        LinkableItem channelDestination = event.getChannelDestination();
        String blackDuckIssueStatus = mapOperationToAlertStatus(event.getOperation());
        return new BlackDuckProviderIssueModel(channelDestination.getValue(), blackDuckIssueStatus, event.getChannelActionSummary(), channelDestination.getUrl().orElse(null));
    }

    private String mapOperationToAlertStatus(IssueOperation issueOperation) {
        switch (issueOperation) {
            case OPEN:
            case UPDATE:
                return "Created by Alert";
            case RESOLVE:
                return "Resolved by Alert";
            default:
                return "Unknown";
        }
    }

}
