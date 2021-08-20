package com.synopsys.integration.alert.provider.blackduck.processor.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.processor.api.extract.model.ProviderDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.BomComponentDetails;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentUpgradeGuidance;
import com.synopsys.integration.alert.processor.api.extract.model.project.ComponentVulnerabilities;
import com.synopsys.integration.alert.processor.api.extract.model.project.MessageReason;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.provider.blackduck.processor.NotificationExtractorBlackDuckServicesFactoryCache;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreator;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BlackDuckMessageBomComponentDetailsCreatorFactory;
import com.synopsys.integration.alert.provider.blackduck.processor.message.service.BomComponent404Handler;
import com.synopsys.integration.alert.provider.blackduck.processor.model.BomEditWithProjectNameNotificationContent;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentVersionView;
import com.synopsys.integration.blackduck.api.manual.component.BomEditNotificationContent;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class BomEditNotificationMessageExtractorTest {
    private static final ProviderDetails PROVIDER_DETAILS = new ProviderDetails(15L, new LinkableItem("Provider", "A provider", "https://provider-url"));
    private static final LinkableItem PROJECT_ITEM = new LinkableItem("Project", "A project", "https://project-url");
    private static final LinkableItem PROJECT_VERSION_ITEM = new LinkableItem("Project Version", "2.3.4-RC", "https://project-version-url");

    private static final LinkableItem COMPONENT = new LinkableItem("Component", "A BOM component");
    private static final LinkableItem COMPONENT_VERSION = new LinkableItem("Component Version", "0.8.7");
    private static final ComponentVulnerabilities COMPONENT_VULNERABILITIES = ComponentVulnerabilities.none();
    private static final LinkableItem LICENSE = new LinkableItem("License", "A software license", "https://license-url");
    private static final String USAGE = "Some generic usage";
    private static final String ISSUES_URL = "https://issues-url";

    private final BomComponentDetails bomComponentDetails = createBomComponentDetails();

    private final BlackDuckProviderKey providerKey = new BlackDuckProviderKey();
    private final NotificationExtractorBlackDuckServicesFactoryCache servicesFactoryCache = Mockito.mock(NotificationExtractorBlackDuckServicesFactoryCache.class);
    private final BlackDuckMessageBomComponentDetailsCreatorFactory detailsCreatorFactory = Mockito.mock(BlackDuckMessageBomComponentDetailsCreatorFactory.class);
    private final BomComponent404Handler bomComponent404Handler = new BomComponent404Handler();

    @Test
    public void createProjectMessageTest() {
        BomEditNotificationMessageExtractor bomEditNotificationMessageExtractor = new BomEditNotificationMessageExtractor(providerKey, servicesFactoryCache, detailsCreatorFactory, bomComponent404Handler);

        ProjectMessage projectMessage = bomEditNotificationMessageExtractor.createProjectMessage(PROVIDER_DETAILS, PROJECT_ITEM, PROJECT_VERSION_ITEM, List.of(bomComponentDetails));

        assertEquals(MessageReason.COMPONENT_UPDATE, projectMessage.getMessageReason());
        assertTrue(projectMessage.getBomComponents().contains(bomComponentDetails));
    }

    @Test
    public void createBomComponentDetailsTest() throws IntegrationException {
        BomEditNotificationMessageExtractor bomEditNotificationMessageExtractor = new BomEditNotificationMessageExtractor(providerKey, servicesFactoryCache, detailsCreatorFactory, bomComponent404Handler);
        BomEditWithProjectNameNotificationContent notificationContent = createBomEditWithProjectNameNotificationContent();
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactoryWithMocks();

        BlackDuckMessageBomComponentDetailsCreator bomComponentDetailsCreator = Mockito.mock(BlackDuckMessageBomComponentDetailsCreator.class);
        Mockito.when(detailsCreatorFactory.createBomComponentDetailsCreator(Mockito.any())).thenReturn(bomComponentDetailsCreator);
        Mockito.when(bomComponentDetailsCreator.createBomComponentDetails(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyList())).thenReturn(bomComponentDetails);

        List<BomComponentDetails> bomComponentDetailsList = bomEditNotificationMessageExtractor.createBomComponentDetails(notificationContent, blackDuckServicesFactory);
        assertTrue(bomComponentDetailsList.contains(bomComponentDetails));
    }

    @Test
    public void createBomComponentDetailsExceptionTest() throws IntegrationException {
        BomEditNotificationMessageExtractor bomEditNotificationMessageExtractor = new BomEditNotificationMessageExtractor(providerKey, servicesFactoryCache, detailsCreatorFactory, bomComponent404Handler);
        BomEditWithProjectNameNotificationContent notificationContent = createBomEditWithProjectNameNotificationContent();
        BlackDuckServicesFactory blackDuckServicesFactory = createBlackDuckServicesFactoryWithMocks();

        BlackDuckMessageBomComponentDetailsCreator bomComponentDetailsCreator = Mockito.mock(BlackDuckMessageBomComponentDetailsCreator.class);
        Mockito.when(detailsCreatorFactory.createBomComponentDetailsCreator(Mockito.any())).thenReturn(bomComponentDetailsCreator);

        Mockito.doThrow(new IntegrationRestException(HttpMethod.GET, new HttpUrl("https://google.com"), HttpStatus.NOT_FOUND.value(), "httpStatusMessageTest", "httpResponseContentTest", "IntegrationRestExceptionForAlertTest"))
            .when(bomComponentDetailsCreator).createBomComponentDetails(Mockito.any(), Mockito.anyList(), Mockito.any(), Mockito.anyList());

        BomComponentDetails bomComponentDetails = createBomComponentDetails();
        Mockito.when(bomComponentDetailsCreator.createMissingBomComponentDetails(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyList(), Mockito.any(), Mockito.anyList()))
            .thenReturn(bomComponentDetails);

        List<BomComponentDetails> bomComponentDetailsList = bomEditNotificationMessageExtractor.createBomComponentDetails(notificationContent, blackDuckServicesFactory);
        assertTrue(bomComponentDetailsList.contains(bomComponentDetails));
    }

    private BlackDuckServicesFactory createBlackDuckServicesFactoryWithMocks() throws IntegrationException {
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);

        ProjectVersionComponentVersionView projectVersionComponentVersionView = new ProjectVersionComponentVersionView();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(), Mockito.any())).thenReturn(projectVersionComponentVersionView);

        return blackDuckServicesFactory;
    }

    private BomEditWithProjectNameNotificationContent createBomEditWithProjectNameNotificationContent() {
        BomEditNotificationContent bomEditNotificationContent = new BomEditNotificationContent();
        bomEditNotificationContent.setProjectVersion(PROJECT_VERSION_ITEM.getLabel());
        bomEditNotificationContent.setBomComponent("http://bomComponentUrl");
        bomEditNotificationContent.setComponentName(COMPONENT.getLabel());
        bomEditNotificationContent.setComponentVersionName(COMPONENT_VERSION.getLabel());

        return new BomEditWithProjectNameNotificationContent(bomEditNotificationContent, PROJECT_ITEM.getLabel(), PROJECT_VERSION_ITEM.getLabel());
    }

    private BomComponentDetails createBomComponentDetails() {
        return new BomComponentDetails(
            COMPONENT,
            COMPONENT_VERSION,
            COMPONENT_VULNERABILITIES,
            List.of(),
            List.of(),
            LICENSE,
            USAGE,
            ComponentUpgradeGuidance.none(),
            List.of(),
            ISSUES_URL
        );
    }
}
