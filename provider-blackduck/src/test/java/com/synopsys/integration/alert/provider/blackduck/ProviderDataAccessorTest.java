package com.synopsys.integration.alert.provider.blackduck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.blackduck.service.dataservice.UserService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

class ProviderDataAccessorTest {
    private static final String PROVIDER_CONFIG_NAME = "Test Black Duck configuration";
    private static final String HREF_1 = "http://localhost";

    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    private BlackDuckPropertiesFactory blackDuckPropertiesFactory;
    private ApiDiscovery apiDiscovery;
    private ProjectService projectService;
    private BlackDuckApiClient blackDuckApiClient;
    private ProjectUsersService projectUsersService;
    private BlackDuckServicesFactory blackDuckServicesFactory;

    private ConfigurationModel providerConfiguration;

    @BeforeEach
    void init() throws Exception {
        blackDuckPropertiesFactory = Mockito.mock(BlackDuckPropertiesFactory.class);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckPropertiesFactory.createProperties(Mockito.any(ConfigurationModel.class))).thenReturn(blackDuckProperties);
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Mockito.when(blackDuckProperties.createBlackDuckHttpClient(Mockito.any(IntLogger.class))).thenReturn(blackDuckHttpClient);
        blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        Mockito.when(blackDuckProperties.createBlackDuckServicesFactory(Mockito.any(BlackDuckHttpClient.class), Mockito.any(IntLogger.class))).thenReturn(blackDuckServicesFactory);
        projectService = Mockito.mock(ProjectService.class);
        Mockito.when(blackDuckServicesFactory.createProjectService()).thenReturn(projectService);
        blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckApiClient()).thenReturn(blackDuckApiClient);
        projectUsersService = Mockito.mock(ProjectUsersService.class);
        Mockito.when(blackDuckServicesFactory.createProjectUsersService()).thenReturn(projectUsersService);

        apiDiscovery = Mockito.mock(ApiDiscovery.class);
        UrlMultipleResponses<UserView> usersLink = Mockito.mock(UrlMultipleResponses.class);
        Mockito.when(apiDiscovery.metaUsersLink()).thenReturn(usersLink);
        Mockito.when(blackDuckServicesFactory.getApiDiscovery()).thenReturn(apiDiscovery);

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        configurationFieldModel.setFieldValue(PROVIDER_CONFIG_NAME);
        configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        providerConfiguration = new ConfigurationModel(1L, 1L, "createdAt", "lastModified", ConfigContextEnum.GLOBAL, Map.of(PROVIDER_CONFIG_NAME, configurationFieldModel));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationById(1L)).thenReturn(Optional.of(providerConfiguration));
        Mockito.when(configurationModelConfigurationAccessor.getProviderConfigurationByName(Mockito.anyString())).thenReturn(Optional.of(providerConfiguration));
    }

    @Test
    void getEmailAddressesForProjectHrefTest() throws Exception {
        Long providerConfigId = providerConfiguration.getConfigurationId();

        ProjectView projectView = createProjectView1();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectView.class))).thenReturn(projectView);

        Set<UserView> userViews = createUserViews();
        Mockito.when(projectUsersService.getAllActiveUsersForProject(Mockito.any(ProjectView.class))).thenReturn(userViews);

        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        Set<String> foundEmailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(providerConfigId, HREF_1);
        assertEquals(3, foundEmailAddresses.size());
    }

    @Test
    void getEmailAddressesForNonExistentProjectHrefTest() throws Exception {
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectView.class))).thenThrow(new IntegrationException("Could not find the project."));

        Long providerConfigId = providerConfiguration.getConfigurationId();
        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        Set<String> foundEmailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(providerConfigId, "expecting no results");
        assertEquals(0, foundEmailAddresses.size());
    }

    @Test
    void getAllUsersTest() throws Exception {
        Long providerConfigId = providerConfiguration.getConfigurationId();

        List<UserView> userViews = new ArrayList<>(createUserViews());
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.any(UrlMultipleResponses.class))).thenReturn(userViews);

        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        List<ProviderUserModel> allProviderUsers = providerDataAccessor.getUsersByProviderConfigId(providerConfigId);
        assertEquals(3, allProviderUsers.size());
    }

    @Test
    void getAllUsersByConfigNameTest() throws Exception {
        List<UserView> userViews = new ArrayList<>(createUserViews());
        Mockito.when(blackDuckApiClient.getAllResponses(Mockito.any(UrlMultipleResponses.class))).thenReturn(userViews);

        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        List<ProviderUserModel> allProviderUsers = providerDataAccessor.getUsersByProviderConfigName(PROVIDER_CONFIG_NAME);
        assertEquals(3, allProviderUsers.size());
    }

    @Test
    void getAllProjectsTest() throws Exception {
        Long providerConfigId = providerConfiguration.getConfigurationId();

        List<ProjectView> projectViews = createProjectViews();
        Mockito.when(projectService.getAllProjects()).thenReturn(projectViews);

        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        List<ProviderProject> allProviderUsers = providerDataAccessor.getProjectsByProviderConfigId(providerConfigId);
        assertEquals(projectViews.size(), allProviderUsers.size());
    }

    @Test
    void getAllProjectsByConfigNameTest() throws Exception {
        List<ProjectView> projectViews = createProjectViews();
        Mockito.when(projectService.getAllProjects()).thenReturn(projectViews);

        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        List<ProviderProject> allProviderUsers = providerDataAccessor.getProjectsByProviderConfigName(PROVIDER_CONFIG_NAME);
        assertEquals(projectViews.size(), allProviderUsers.size());
    }

    @Test
    void getProjectByHrefTest() throws IntegrationException {
        ProjectView projectView = createProjectView1();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectView.class))).thenReturn(projectView);
        BlackDuckProviderDataAccessor blackDuckProviderDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        Optional<ProviderProject> projectByHref = blackDuckProviderDataAccessor.getProjectByHref(providerConfiguration.getConfigurationId(), HREF_1);

        assertTrue(projectByHref.isPresent());
        ProviderProject providerProject = projectByHref.get();

        assertEquals(HREF_1, providerProject.getHref());
    }

    @Test
    void getProjectVersionsByHrefTest() throws IntegrationException {
        ProjectView projectView = createProjectViewWithVersionsLink();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectView.class))).thenReturn(projectView);

        ProjectVersionView projectVersionView = new ProjectVersionView();
        String versionName = "versionName";
        int totalPages = 1;
        projectVersionView.setVersionName(versionName);
        BlackDuckPageResponse blackDuckPageResponse = new BlackDuckPageResponse(totalPages, List.of(projectVersionView));
        Mockito.when(blackDuckApiClient.getPageResponse(Mockito.any())).thenReturn(blackDuckPageResponse);

        BlackDuckProviderDataAccessor blackDuckProviderDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        AlertPagedModel<String> projectVersionNamesByHref = blackDuckProviderDataAccessor.getProjectVersionNamesByHref(1L, HREF_1, 1);

        assertNotNull(projectVersionNamesByHref);
        assertEquals(totalPages, projectVersionNamesByHref.getTotalPages());

        Optional<String> firstFoundProjectVersion = projectVersionNamesByHref.getModels().stream().findFirst();
        assertTrue(firstFoundProjectVersion.isPresent());

        String foundVersionName = firstFoundProjectVersion.get();
        assertEquals(versionName, foundVersionName);
    }

    @Test
    void getProjectVersionsByHrefWithNoConfigTest() throws IntegrationException {
        ProjectView projectView = createProjectViewWithVersionsLink();
        Mockito.when(blackDuckApiClient.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectView.class))).thenReturn(projectView);

        ProjectVersionView projectVersionView = new ProjectVersionView();
        String versionName = "versionName";
        int totalPages = 1;
        projectVersionView.setVersionName(versionName);
        BlackDuckPageResponse blackDuckPageResponse = new BlackDuckPageResponse(totalPages, List.of(projectVersionView));
        Mockito.when(blackDuckApiClient.getPageResponse(Mockito.any())).thenReturn(blackDuckPageResponse);

        BlackDuckProviderDataAccessor blackDuckProviderDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        AlertPagedModel<String> projectVersionNamesByHref = blackDuckProviderDataAccessor.getProjectVersionNamesByHref(999L, HREF_1, 1);

        assertNotNull(projectVersionNamesByHref);
        assertEquals(0, projectVersionNamesByHref.getTotalPages());
    }

    @Test
    void getProviderConfigUserByIdTest() throws IntegrationException {
        UserService userService = Mockito.mock(UserService.class);
        Mockito.when(blackDuckServicesFactory.createUserService()).thenReturn(userService);

        String emailAddress = "fake@email.address";
        UserView userView1 = new UserView();
        userView1.setActive(true);
        userView1.setEmail(emailAddress);
        Mockito.when(userService.findCurrentUser()).thenReturn(userView1);

        BlackDuckProviderDataAccessor blackDuckProviderDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        ProviderUserModel providerConfigUser = blackDuckProviderDataAccessor.getProviderConfigUserById(1L);

        assertNotNull(providerConfigUser);
        assertEquals(emailAddress, providerConfigUser.getEmailAddress());
    }

    @Test
    void getProviderConfigUserByIdNotFoundTest() throws IntegrationException {
        UserService userService = Mockito.mock(UserService.class);
        Mockito.when(blackDuckServicesFactory.createUserService()).thenReturn(userService);

        String emailAddress = "fake@email.address";
        UserView userView1 = new UserView();
        userView1.setActive(true);
        userView1.setEmail(emailAddress);
        Mockito.when(userService.findCurrentUser()).thenReturn(userView1);

        BlackDuckProviderDataAccessor blackDuckProviderDataAccessor = new BlackDuckProviderDataAccessor(configurationModelConfigurationAccessor, blackDuckPropertiesFactory);
        long nonExistentConfig = 999L;
        try {
            ProviderUserModel providerConfigUser = blackDuckProviderDataAccessor.getProviderConfigUserById(nonExistentConfig);
            fail();
        } catch (AlertConfigurationException exception) {
            assertTrue(exception.getMessage().contains(String.valueOf(nonExistentConfig)));
        }
    }

    private Set<UserView> createUserViews() {
        String emailAddress1 = "someone@gmail.com";
        String emailAddress2 = "someoneelse@gmail.com";
        String emailAddress3 = "other@gmail.com";

        UserView userView1 = new UserView();
        userView1.setActive(true);
        userView1.setEmail(emailAddress1);

        UserView userView2 = new UserView();
        userView2.setActive(true);
        userView2.setEmail(emailAddress2);

        UserView userView3 = new UserView();
        userView3.setActive(true);
        userView3.setEmail(emailAddress3);

        UserView userView4 = new UserView();
        userView4.setActive(false);
        userView4.setEmail(emailAddress1);

        UserView userView5 = new UserView();
        userView5.setActive(true);
        userView5.setEmail(emailAddress2);

        Set<UserView> userViews = new HashSet<>();
        userViews.add(userView1);
        userViews.add(userView2);
        userViews.add(userView3);
        userViews.add(userView4);
        userViews.add(userView5);
        return userViews;
    }

    private ProjectView createProjectView1() throws IntegrationException {
        ProjectView projectView = new ProjectView();
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(new HttpUrl(HREF_1));
        projectView.setMeta(resourceMetadata);

        return projectView;
    }

    private ProjectView createProjectView2() throws IntegrationException {
        String href2 = "https://localhost:8443";
        ProjectView projectView2 = new ProjectView();
        ResourceMetadata resourceMetadata2 = new ResourceMetadata();
        resourceMetadata2.setHref(new HttpUrl(href2));
        projectView2.setMeta(resourceMetadata2);

        return projectView2;
    }

    private ProjectView createProjectViewWithVersionsLink() throws IntegrationException {
        ProjectView projectView = new ProjectView();
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(new HttpUrl(HREF_1));
        ResourceLink versionLink = new ResourceLink();
        versionLink.setRel(ProjectView.VERSIONS_LINK);
        versionLink.setHref(new HttpUrl("https://versionsHref.com"));
        resourceMetadata.setLinks(List.of(versionLink));
        projectView.setMeta(resourceMetadata);

        return projectView;
    }

    private List<ProjectView> createProjectViews() throws Exception {
        return List.of(createProjectView1(), createProjectView2());
    }

}
