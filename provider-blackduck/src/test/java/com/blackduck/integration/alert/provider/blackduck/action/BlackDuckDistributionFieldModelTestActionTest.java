package com.blackduck.integration.alert.provider.blackduck.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.api.provider.state.StatefulProvider;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackduck.integration.alert.provider.blackduck.BlackDuckProvider;
import com.blackduck.integration.alert.provider.blackduck.validator.BlackDuckSystemValidator;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.blackduck.http.client.ApiTokenBlackDuckHttpClient;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.rest.response.DefaultResponse;

class BlackDuckDistributionFieldModelTestActionTest {
    private static final String PROJECT_1_HREF = "href";
    private static final String PROJECT_2_HREF = "project 2 href";
    private static final String PROJECT_3_HREF = "project 3 href";
    private static final String PROJECT_4_HREF = "project 4 href";

    @Test
    void testCannotConnectToProvider() throws IntegrationException {
        FieldUtility fieldUtility = createFieldUtilityData(null, "1.0.*");
        BlackDuckDistributionFieldModelTestAction testAction = createTestAction(false);
        MessageResult messageResult = testAction.testConfig(null, null, fieldUtility);

        assertNotNull(messageResult);
        assertTrue(messageResult.hasErrors(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.ERROR).toString());
        assertFalse(messageResult.hasWarnings(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.WARNING).toString());
    }

    @Test
    void testConfigWithOnlyProjectVersionNamePatternTest() throws IntegrationException {
        FieldUtility fieldUtility = createFieldUtilityData(null, "1.0.*");
        BlackDuckDistributionFieldModelTestAction testAction = createTestAction(true);
        MessageResult messageResult = testAction.testConfig(null, null, fieldUtility);

        assertNotNull(messageResult);
        assertFalse(messageResult.hasErrors(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.ERROR).toString());
        assertFalse(messageResult.hasWarnings(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.WARNING).toString());
    }

    @Test
    void testConfigWithProjectVersionAndProjectNamePatternsTest() throws IntegrationException {
        FieldUtility fieldUtility = createFieldUtilityData("na.*", "1.0.*");
        BlackDuckDistributionFieldModelTestAction testAction = createTestAction(true);
        MessageResult messageResult = testAction.testConfig(null, null, fieldUtility);

        assertNotNull(messageResult);
        assertFalse(messageResult.hasErrors(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.ERROR).toString());
        assertFalse(messageResult.hasWarnings(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.WARNING).toString());
    }

    @Test
    void testConfigWithProjectVersionAndProjectNamePatternsNotMatchingTest() throws IntegrationException {
        FieldUtility fieldUtility = createFieldUtilityData("fake*", "wrong");
        BlackDuckDistributionFieldModelTestAction testAction = createTestAction(true);
        MessageResult messageResult = testAction.testConfig(null, null, fieldUtility);

        assertNotNull(messageResult);
        assertFalse(messageResult.hasErrors(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.ERROR).toString());
        assertTrue(messageResult.hasWarnings(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.WARNING).toString());
        assertEquals(2, messageResult.getFieldStatuses().size(), messageResult.getFieldStatuses().toString());
    }

    @Test
    void testConfigWithProjectVersionNamePatternNotMatchingTest() throws IntegrationException {
        FieldUtility fieldUtility = createFieldUtilityData(null, "wrong");
        BlackDuckDistributionFieldModelTestAction testAction = createTestAction(true);
        MessageResult messageResult = testAction.testConfig(null, null, fieldUtility);

        assertNotNull(messageResult);
        assertFalse(messageResult.hasErrors(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.ERROR).toString());
        assertTrue(messageResult.hasWarnings(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.WARNING).toString());
        assertEquals(1, messageResult.getFieldStatuses().size(), messageResult.getFieldStatuses().toString());
    }

    @Test
    void testConfigWithProjectNamePatternNotMatchingTest() throws IntegrationException {
        FieldUtility fieldUtility = createFieldUtilityData("fake*", null);
        BlackDuckDistributionFieldModelTestAction testAction = createTestAction(true);
        MessageResult messageResult = testAction.testConfig(null, null, fieldUtility);

        assertNotNull(messageResult);
        assertFalse(messageResult.hasErrors(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.ERROR).toString());
        assertTrue(messageResult.hasWarnings(), messageResult.getFieldStatusesBySeverity(FieldStatusSeverity.WARNING).toString());
        assertEquals(1, messageResult.getFieldStatuses().size(), messageResult.getFieldStatuses().toString());
    }

    @Disabled
    @Test
    @Tag(TestTags.DEFAULT_PERFORMANCE)
    void verifyProjectNameVersionPatternCheckIsFasterWithMatchingProjectNamePatternTest() throws IntegrationException {
        FieldUtility fieldUtility = createFieldUtilityData(null, "0.0.*");
        FieldUtility fasterFieldUtility = createFieldUtilityData("project 2*", "0.0.*");
        BlackDuckDistributionFieldModelTestAction testAction = createTestAction(true);

        long startTest1 = System.nanoTime();
        MessageResult messageResult = testAction.testConfig(null, null, fieldUtility);
        long endTest1 = System.nanoTime();
        long slowTime = endTest1 - startTest1;

        long startTest2 = System.nanoTime();
        MessageResult fasterResult = testAction.testConfig(null, null, fasterFieldUtility);
        long endTest2 = System.nanoTime();
        long fastTime = endTest2 - startTest2;

        assertEquals(messageResult, fasterResult);
        assertTrue(fastTime < slowTime, "config with project name pattern should be faster: Fast time " + fastTime + " Slow time " + slowTime);
    }

    private BlackDuckDistributionFieldModelTestAction createTestAction(boolean canConnectToProvider) throws IntegrationException {
        ProviderDataAccessor providerDataAccessor = createProviderDataAccessor();
        ConfigurationModelConfigurationAccessor configurationAccessor = createConfigurationAccessor();
        BlackDuckSystemValidator blackDuckSystemValidator = new BlackDuckSystemValidator(null);
        BlackDuckProvider blackDuckProvider = createBlackDuckProvider(canConnectToProvider);
        return new BlackDuckDistributionFieldModelTestAction(providerDataAccessor, blackDuckProvider, configurationAccessor, blackDuckSystemValidator);
    }

    private ProviderDataAccessor createProviderDataAccessor() {
        ProviderDataAccessor providerDataAccessor = Mockito.mock(ProviderDataAccessor.class);
        Mockito.when(providerDataAccessor.getProjectsByProviderConfigId(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString())).thenReturn(createProviderProjects());
        createHrefsToVersionsMap().entrySet()
            .forEach(entry -> Mockito.when(providerDataAccessor.getProjectVersionNamesByHref(1L, entry.getKey(), 0)).thenReturn(entry.getValue()));

        return providerDataAccessor;
    }

    private AlertPagedModel<ProviderProject> createProviderProjects() {
        List<ProviderProject> providerProjects = List.of(
            new ProviderProject("name", "description", PROJECT_1_HREF, "projectOwnerEmail"),
            new ProviderProject("project 3", null, PROJECT_3_HREF, null),
            new ProviderProject("project 4", null, PROJECT_4_HREF, null),
            new ProviderProject("project 2", null, PROJECT_2_HREF, null)
        );

        return new AlertPagedModel<>(1, 0, 100, providerProjects);
    }

    private Map<String, AlertPagedModel<String>> createHrefsToVersionsMap() {
        return Map.of(
            PROJECT_1_HREF, new AlertPagedModel<>(1, 0, 100, List.of("1.0.0", "1.1.0", "2.0.0", "3.0.0", "3.0.1")),
            PROJECT_2_HREF, new AlertPagedModel<>(1, 0, 100, List.of("0.0.1", "0.1.0", "1.0.0")),
            PROJECT_3_HREF, new AlertPagedModel<>(1, 0, 100, List.of("SNAPSHOT")),
            PROJECT_4_HREF, new AlertPagedModel<>(1, 0, 100, List.of("1.0.0", "1.0.1", "1.0.2", "1.0.3", "1.0.4", "1.1.0", "1.1.1-SNAPSHOT"))
        );
    }

    private ConfigurationModelConfigurationAccessor createConfigurationAccessor() {
        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(Mockito.anyLong())).thenReturn(Optional.of(configurationModel));
        return configurationModelConfigurationAccessor;
    }

    private BlackDuckProvider createBlackDuckProvider(boolean canConnect) throws IntegrationException {
        BlackDuckProvider blackDuckProvider = Mockito.mock(BlackDuckProvider.class);
        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        ApiTokenBlackDuckHttpClient apiTokenBlackDuckHttpClient = Mockito.mock(ApiTokenBlackDuckHttpClient.class);
        DefaultResponse httpResponse = Mockito.mock(DefaultResponse.class);
        Mockito.when(blackDuckProvider.createStatefulProvider(Mockito.any())).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getProperties()).thenReturn(blackDuckProperties);
        Mockito.when(blackDuckProperties.createApiTokenBlackDuckHttpClient(Mockito.any())).thenReturn(apiTokenBlackDuckHttpClient);
        Mockito.when(httpResponse.isStatusCodeSuccess()).thenReturn(canConnect);
        Mockito.when(apiTokenBlackDuckHttpClient.attemptAuthentication()).thenReturn(httpResponse);
        Mockito.when(blackDuckProperties.getBlackDuckUrl()).thenReturn(Optional.of("https://synopsys.com"));
        return blackDuckProvider;
    }

    private FieldUtility createFieldUtilityData(String projectNamePattern, String projectVersionNamePattern) {
        ConfigurationFieldModel providerConfigId = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
        providerConfigId.setFieldValue("1");
        ConfigurationFieldModel filterByProject = ConfigurationFieldModel.create(ProviderDescriptor.KEY_FILTER_BY_PROJECT);
        filterByProject.setFieldValue("true");
        ConfigurationFieldModel configuredProject = ConfigurationFieldModel.create(ProviderDescriptor.KEY_CONFIGURED_PROJECT);
        ConfigurationFieldModel projectNamePatternField = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROJECT_NAME_PATTERN);
        projectNamePatternField.setFieldValue(projectNamePattern);
        ConfigurationFieldModel projectVersionNamePatternField = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROJECT_VERSION_NAME_PATTERN);
        projectVersionNamePatternField.setFieldValue(projectVersionNamePattern);
        Map<String, ConfigurationFieldModel> fieldModelMap = DataStructureUtils.mapToValues(
            List.of(providerConfigId, filterByProject, configuredProject, projectNamePatternField, projectVersionNamePatternField),
            ConfigurationFieldModel::getFieldKey
        );

        return new FieldUtility(fieldModelMap);
    }
}
