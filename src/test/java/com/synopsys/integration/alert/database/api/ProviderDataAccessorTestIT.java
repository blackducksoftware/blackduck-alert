package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.descriptor.api.model.ProviderKey;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderDataAccessor;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.BlackDuckService;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.blackduck.service.dataservice.ProjectService;
import com.synopsys.integration.blackduck.service.dataservice.ProjectUsersService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class ProviderDataAccessorTestIT extends AlertIntegrationTest {
    @Autowired
    private ConfigurationAccessor configurationAccessor;
    @Autowired
    private BlackDuckProviderKey blackDuckProviderKey;

    private BlackDuckPropertiesFactory blackDuckPropertiesFactory;
    private ProjectService projectService;
    private BlackDuckService blackDuckService;
    private ProjectUsersService projectUsersService;

    private ConfigurationModel providerConfiguration;
    private String providerConfigurationName = "Test Black Duck configuration";

    @BeforeEach
    public void init() throws Exception {
        blackDuckPropertiesFactory = Mockito.mock(BlackDuckPropertiesFactory.class);
        BlackDuckProperties blackDuckProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(blackDuckPropertiesFactory.createProperties(Mockito.any(ConfigurationModel.class))).thenReturn(blackDuckProperties);
        BlackDuckHttpClient blackDuckHttpClient = Mockito.mock(BlackDuckHttpClient.class);
        Mockito.when(blackDuckProperties.createBlackDuckHttpClient(Mockito.any(IntLogger.class))).thenReturn(blackDuckHttpClient);
        BlackDuckServicesFactory blackDuckServicesFactory = Mockito.mock(BlackDuckServicesFactory.class);
        Mockito.when(blackDuckProperties.createBlackDuckServicesFactory(Mockito.any(BlackDuckHttpClient.class), Mockito.any(IntLogger.class))).thenReturn(blackDuckServicesFactory);
        projectService = Mockito.mock(ProjectService.class);
        Mockito.when(blackDuckServicesFactory.createProjectService()).thenReturn(projectService);
        blackDuckService = Mockito.mock(BlackDuckService.class);
        Mockito.when(blackDuckServicesFactory.getBlackDuckService()).thenReturn(blackDuckService);
        projectUsersService = Mockito.mock(ProjectUsersService.class);
        Mockito.when(blackDuckServicesFactory.createProjectUsersService()).thenReturn(projectUsersService);

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        configurationFieldModel.setFieldValue(providerConfigurationName);
        providerConfiguration = configurationAccessor.createConfiguration(blackDuckProviderKey, ConfigContextEnum.GLOBAL, List.of(configurationFieldModel));
    }

    @AfterEach
    public void cleanup() throws Exception {
        configurationAccessor.deleteConfiguration(providerConfiguration);
    }

    private ProviderKey createProviderKey(String key) {
        return new ProviderKey(key, key);
    }

    @Test
    public void getEmailAddressesForProjectHrefTest() throws Exception {
        Long providerConfigId = providerConfiguration.getConfigurationId();

        String href = "http://localhost";

        ProjectView projectView = new ProjectView();
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(new HttpUrl(href));
        projectView.setMeta(resourceMetadata);
        Mockito.when(blackDuckService.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectView.class))).thenReturn(projectView);

        Set<UserView> userViews = createUserViews();
        Mockito.when(projectUsersService.getAllActiveUsersForProject(Mockito.any(ProjectView.class))).thenReturn(userViews);

        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationAccessor, blackDuckPropertiesFactory);
        Set<String> foundEmailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(providerConfigId, href);
        assertEquals(3, foundEmailAddresses.size());
    }

    @Test
    public void getEmailAddressesForNonExistentProjectHrefTest() throws Exception {
        Mockito.when(blackDuckService.getResponse(Mockito.any(HttpUrl.class), Mockito.eq(ProjectView.class))).thenThrow(new IntegrationException("Could not find the project."));

        Long providerConfigId = providerConfiguration.getConfigurationId();
        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationAccessor, blackDuckPropertiesFactory);
        Set<String> foundEmailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(providerConfigId, "expecting no results");
        assertEquals(0, foundEmailAddresses.size());
    }

    @Test
    public void getAllUsersTest() throws Exception {
        Long providerConfigId = providerConfiguration.getConfigurationId();

        List<UserView> userViews = new ArrayList<>(createUserViews());
        Mockito.when(blackDuckService.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(userViews);

        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationAccessor, blackDuckPropertiesFactory);
        List<ProviderUserModel> allProviderUsers = providerDataAccessor.getUsersByProviderConfigId(providerConfigId);
        assertEquals(3, allProviderUsers.size());
    }

    @Test
    public void getAllUsersByConfigNameTest() throws Exception {
        List<UserView> userViews = new ArrayList<>(createUserViews());
        Mockito.when(blackDuckService.getAllResponses(Mockito.any(BlackDuckPathMultipleResponses.class))).thenReturn(userViews);

        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationAccessor, blackDuckPropertiesFactory);
        List<ProviderUserModel> allProviderUsers = providerDataAccessor.getUsersByProviderConfigName(providerConfigurationName);
        assertEquals(3, allProviderUsers.size());
    }

    @Test
    public void getAllProjectsTest() throws Exception {
        Long providerConfigId = providerConfiguration.getConfigurationId();

        List<ProjectView> projectViews = createProjectViews();
        Mockito.when(projectService.getAllProjects()).thenReturn(projectViews);

        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationAccessor, blackDuckPropertiesFactory);
        List<ProviderProject> allProviderUsers = providerDataAccessor.getProjectsByProviderConfigId(providerConfigId);
        assertEquals(projectViews.size(), allProviderUsers.size());
    }

    @Test
    public void getAllProjectsByConfigNameTest() throws Exception {
        List<ProjectView> projectViews = createProjectViews();
        Mockito.when(projectService.getAllProjects()).thenReturn(projectViews);

        BlackDuckProviderDataAccessor providerDataAccessor = new BlackDuckProviderDataAccessor(configurationAccessor, blackDuckPropertiesFactory);
        List<ProviderProject> allProviderUsers = providerDataAccessor.getProjectsByProviderConfigName(providerConfigurationName);
        assertEquals(projectViews.size(), allProviderUsers.size());
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

    private List<ProjectView> createProjectViews() throws Exception {
        String href1 = "http://localhost";
        String href2 = "https://localhost:8443";

        ProjectView projectView = new ProjectView();
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setHref(new HttpUrl(href1));
        projectView.setMeta(resourceMetadata);

        ProjectView projectView2 = new ProjectView();
        ResourceMetadata resourceMetadata2 = new ResourceMetadata();
        resourceMetadata2.setHref(new HttpUrl(href2));
        projectView2.setMeta(resourceMetadata2);

        return List.of(projectView, projectView2);
    }

}
