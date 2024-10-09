package com.blackduck.integration.alert.channel.email.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.blackduck.integration.alert.channel.email.attachment.compatibility.ComponentItem;
import com.blackduck.integration.alert.channel.email.attachment.compatibility.MessageContentGroup;
import com.blackduck.integration.alert.channel.email.attachment.compatibility.ProviderMessageContent;
import com.blackduck.integration.alert.api.processor.extract.model.ProviderDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.BomComponentDetails;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcern;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentConcernSeverity;
import com.blackduck.integration.alert.api.processor.extract.model.project.ComponentUpgradeGuidance;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.blackduck.integration.alert.api.processor.extract.model.project.ProjectOperation;
import com.blackduck.integration.alert.common.enumeration.ItemOperation;
import com.blackduck.integration.alert.common.message.model.LinkableItem;

@ExtendWith(SpringExtension.class)
class ProjectMessageToMessageContentGroupConversionUtilsTest {
    @Mock
    BomComponentDetails mockBomComponentDetails;

    private static final LinkableItem VALID_PROVIDER = ProjectMessageToMessageContentGroupConversionUtilsTest.generateLinkableItem("provider");
    private static final ProviderDetails VALID_PROVIDER_DETAILS = new ProviderDetails(123456789L, VALID_PROVIDER);
    private static final LinkableItem VALID_PROJECT = ProjectMessageToMessageContentGroupConversionUtilsTest.generateLinkableItem("project");
    private static final LinkableItem VALID_PROJECT_VERSION = ProjectMessageToMessageContentGroupConversionUtilsTest.generateLinkableItem("projectVersion");

    @Test
    void dataWithNullReturnsExpectedTest() {
        LinkableItem invalidProvider = new LinkableItem(null, "test-provider-value", "test-provider-url");
        ProviderDetails providerDetails = new ProviderDetails(123456789L, invalidProvider);
        ProjectMessage projectMessage = ProjectMessage.projectVersionStatusInfo(providerDetails, VALID_PROJECT, null, ProjectOperation.CREATE);
        MessageContentGroup messageContentGroup = ProjectMessageToMessageContentGroupConversionUtils.toMessageContentGroup(projectMessage);

        assertEquals(0, messageContentGroup.getSubContent().size());
        assertNull(messageContentGroup.getCommonProvider());
        assertNull(messageContentGroup.getCommonTopic());
    }

    @Test
    void noBomComponentsReturnsExpectedTest() {
        ProjectMessage projectMessage = ProjectMessage.projectVersionStatusInfo(VALID_PROVIDER_DETAILS, VALID_PROJECT, VALID_PROJECT_VERSION, ProjectOperation.DELETE);
        MessageContentGroup messageContentGroup = ProjectMessageToMessageContentGroupConversionUtils.toMessageContentGroup(projectMessage);

        assertEquals(VALID_PROVIDER, messageContentGroup.getCommonProvider());
        assertEquals(VALID_PROJECT, messageContentGroup.getCommonTopic());
        assertEquals(1, messageContentGroup.getSubContent().size());

        ProviderMessageContent providerMessageContent = messageContentGroup.getSubContent().get(0);

        assertEquals(ItemOperation.DELETE, providerMessageContent.getAction().orElse(null));
        assertEquals(VALID_PROVIDER, providerMessageContent.getProvider());
        assertEquals(VALID_PROJECT, providerMessageContent.getTopic());
        assertEquals(VALID_PROJECT_VERSION, providerMessageContent.getSubTopic().orElse(null));
        assertTrue(providerMessageContent.getNotificationId().isEmpty());
        assertEquals(0, providerMessageContent.getComponentItems().size());
        assertNull(providerMessageContent.getProviderCreationTime());
        assertEquals(-1L, providerMessageContent.getProviderConfigId());
    }

    @Test
    void validBomComponentsReturnsExpectedTest() {
        LinkableItem expectedCategoryItem = new LinkableItem("Vulnerabilities", "my-test-component-name", "my-test-url");
        LinkableItem expectedCategoryGroupingAttribute = new LinkableItem("Severity", ComponentConcernSeverity.BLOCKER.getPolicyLabel(), null);

        List<ComponentConcern> componentConcerns = List.of(ComponentConcern.unknownComponentVersion(
            ItemOperation.INFO,
            "my-test-component-name",
            ComponentConcernSeverity.BLOCKER,
            5,
            "my-test-url"
        ));

        LinkableItem component = generateLinkableItem("component");

        Mockito.when(mockBomComponentDetails.getComponentConcerns()).thenReturn(componentConcerns);
        Mockito.when(mockBomComponentDetails.getComponent()).thenReturn(component);
        Mockito.when(mockBomComponentDetails.getComponentUpgradeGuidance()).thenReturn(ComponentUpgradeGuidance.none());

        List<BomComponentDetails> bomComponents = List.of(mockBomComponentDetails);
        ProjectMessage projectMessage = ProjectMessage.componentUpdate(VALID_PROVIDER_DETAILS, VALID_PROJECT, VALID_PROJECT_VERSION, bomComponents);
        MessageContentGroup messageContentGroup = ProjectMessageToMessageContentGroupConversionUtils.toMessageContentGroup(projectMessage);

        ProviderMessageContent providerMessageContent = messageContentGroup.getSubContent().get(0);
        assertEquals(1, providerMessageContent.getComponentItems().size());

        ComponentItem componentItem = providerMessageContent.getComponentItems().stream().findFirst().orElse(null);
        assertNotNull((componentItem));

        assertEquals(ItemOperation.INFO, componentItem.getOperation());
        assertEquals(component, componentItem.getComponent());
        assertEquals(expectedCategoryItem, componentItem.getCategoryItem());
        assertEquals(Optional.of(expectedCategoryGroupingAttribute), componentItem.getCategoryGroupingAttribute());
    }

    @Test
    void invalidBomComponentsReturnsExpectedTest() {
        List<ComponentConcern> componentConcerns = List.of(ComponentConcern.policy(
            ItemOperation.UPDATE,
            "my-test-policy-name",
            "my-test-url"
        ));

        Mockito.when(mockBomComponentDetails.getComponentConcerns()).thenReturn(componentConcerns);
        Mockito.when(mockBomComponentDetails.getComponentUpgradeGuidance()).thenReturn(ComponentUpgradeGuidance.none());

        List<BomComponentDetails> bomComponents = List.of(mockBomComponentDetails);
        ProjectMessage projectMessage = ProjectMessage.componentUpdate(VALID_PROVIDER_DETAILS, VALID_PROJECT, VALID_PROJECT_VERSION, bomComponents);
        MessageContentGroup messageContentGroup = ProjectMessageToMessageContentGroupConversionUtils.toMessageContentGroup(projectMessage);

        ProviderMessageContent providerMessageContent = messageContentGroup.getSubContent().get(0);
        assertEquals(0, providerMessageContent.getComponentItems().size());
    }

    static private LinkableItem generateLinkableItem(String keyWord) {
        return new LinkableItem("test-label-" + keyWord, "test-value-" + keyWord, "test-url-" + keyWord);
    }

}
