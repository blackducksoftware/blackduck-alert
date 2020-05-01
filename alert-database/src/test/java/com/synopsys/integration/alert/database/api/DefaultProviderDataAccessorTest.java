package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelation;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelationRepository;
import com.synopsys.integration.alert.database.provider.user.ProviderUserEntity;
import com.synopsys.integration.alert.database.provider.user.ProviderUserRepository;

public class DefaultProviderDataAccessorTest {
    private final String name = "name-test";
    private final String description = "decription-test";
    private final String href = "href-test";
    private final String projectOwnerEmail = "noreply@blackducksoftware.com";

    private final String KEY_PROVIDER_CONFIG_NAME = ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME;
    private final String fieldValue = "test-channel.common.name-value";

    ProviderProject expectedProviderProject = new ProviderProject(name, description, href, projectOwnerEmail);

    @Test
    public void getProjectsByProviderConfigNameTest() throws Exception {
        ConfigurationModel configurationModel = createConfigurationModel();
        ProviderProjectEntity providerProjectEntity = new ProviderProjectEntity(name, description, href, projectOwnerEmail, 1L);

        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ProviderProjectRepository providerProjectRepository = Mockito.mock(ProviderProjectRepository.class);

        Mockito.when(configurationAccessor.getProviderConfigurationByName(Mockito.any())).thenReturn(Optional.of(configurationModel));
        Mockito.when(providerProjectRepository.findByProviderConfigId(Mockito.any())).thenReturn(List.of(providerProjectEntity));

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, null, null, configurationAccessor);
        List<ProviderProject> providerProjectList = providerDataAccessor.getProjectsByProviderConfigName("test-providerConfigName");

        assertEquals(1, providerProjectList.size());
        ProviderProject providerProject = providerProjectList.get(0);
        testProviderProject(expectedProviderProject, providerProject);
    }

    @Test
    public void getProjectsByProviderConfigNameEmptyTest() throws Exception {
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ProviderProjectRepository providerProjectRepository = Mockito.mock(ProviderProjectRepository.class);

        Mockito.when(configurationAccessor.getProviderConfigurationByName(Mockito.any())).thenReturn(Optional.empty());

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, null, null, configurationAccessor);
        List<ProviderProject> providerProjectList = providerDataAccessor.getProjectsByProviderConfigName("test-providerConfigName");

        assertTrue(providerProjectList.isEmpty());
    }

    @Test
    public void getProjectsByProviderConfigIdTest() {
        ProviderProjectEntity providerProjectEntity = new ProviderProjectEntity(name, description, href, projectOwnerEmail, 1L);

        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ProviderProjectRepository providerProjectRepository = Mockito.mock(ProviderProjectRepository.class);

        Mockito.when(providerProjectRepository.findByProviderConfigId(Mockito.any())).thenReturn(List.of(providerProjectEntity));

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, null, null, configurationAccessor);
        List<ProviderProject> providerProjectList = providerDataAccessor.getProjectsByProviderConfigId(1L);

        assertEquals(1, providerProjectList.size());
        ProviderProject providerProject = providerProjectList.get(0);
        testProviderProject(expectedProviderProject, providerProject);
    }

    @Test
    public void deleteProjectsTest() {
        ProviderProject providerProject = new ProviderProject(name, description, href, projectOwnerEmail);

        ProviderProjectRepository providerProjectRepository = Mockito.mock(ProviderProjectRepository.class);

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, null, null, null);
        providerDataAccessor.deleteProjects(List.of(providerProject));

        Mockito.verify(providerProjectRepository).deleteByHref(Mockito.any());
    }

    @Test
    public void getEmailAddressesForProjectHrefTest() throws Exception {
        ProviderProjectEntity providerProjectEntity = new ProviderProjectEntity(name, description, href, projectOwnerEmail, 1L);
        providerProjectEntity.setId(1L);
        ProviderUserProjectRelation providerUserProjectRelation = new ProviderUserProjectRelation(2L, 1L);
        ProviderUserEntity providerUserEntity = new ProviderUserEntity(projectOwnerEmail, true, 1L);

        ProviderProjectRepository providerProjectRepository = Mockito.mock(ProviderProjectRepository.class);
        ProviderUserProjectRelationRepository providerUserProjectRelationRepository = Mockito.mock(ProviderUserProjectRelationRepository.class);
        ProviderUserRepository providerUserRepository = Mockito.mock(ProviderUserRepository.class);

        Mockito.when(providerProjectRepository.findFirstByHref(Mockito.any())).thenReturn(Optional.of(providerProjectEntity));
        Mockito.when(providerUserProjectRelationRepository.findByProviderProjectId(Mockito.any())).thenReturn(List.of(providerUserProjectRelation));
        Mockito.when(providerUserRepository.findAllById(Mockito.any())).thenReturn(List.of(providerUserEntity));

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository, null);
        Set<String> emailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(href);

        assertEquals(1, emailAddresses.size());
        assertTrue(emailAddresses.contains(projectOwnerEmail));
    }

    @Test
    public void getEmailAddressesForProjectHrefEmptyTest() {
        ProviderProjectRepository providerProjectRepository = Mockito.mock(ProviderProjectRepository.class);

        Mockito.when(providerProjectRepository.findFirstByHref(Mockito.any())).thenReturn(Optional.empty());

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, null, null, null);
        Set<String> emailAddresses = providerDataAccessor.getEmailAddressesForProjectHref("test-href");

        assertTrue(emailAddresses.isEmpty());
    }

    @Test
    public void getUsersByProviderConfigIdTest() {
        ProviderUserEntity providerUserEntity = new ProviderUserEntity(projectOwnerEmail, true, 1L);

        ProviderProjectRepository providerProjectRepository = Mockito.mock(ProviderProjectRepository.class);
        ProviderUserRepository providerUserRepository = Mockito.mock(ProviderUserRepository.class);

        Mockito.when(providerUserRepository.findByProviderConfigId(Mockito.any())).thenReturn(List.of(providerUserEntity));

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, null, providerUserRepository, null);
        List<ProviderUserModel> providerUserModelList = providerDataAccessor.getUsersByProviderConfigId(1L);

        assertEquals(1, providerUserModelList.size());
        ProviderUserModel providerUserModel = providerUserModelList.get(0);
        assertEquals(projectOwnerEmail, providerUserModel.getEmailAddress());
        assertTrue(providerUserModel.getOptOut());
    }

    @Test
    public void getUsersByProviderConfigIdNullTest() {
        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(null, null, null, null);
        List<ProviderUserModel> providerUserModelList = providerDataAccessor.getUsersByProviderConfigId(null);

        assertTrue(providerUserModelList.isEmpty());
    }

    @Test
    public void getUsersByProviderConfigNameTest() throws Exception {
        ConfigurationModel configurationModel = createConfigurationModel();
        ProviderUserEntity providerUserEntity = new ProviderUserEntity(projectOwnerEmail, true, 1L);

        ProviderProjectRepository providerProjectRepository = Mockito.mock(ProviderProjectRepository.class);
        ProviderUserRepository providerUserRepository = Mockito.mock(ProviderUserRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(configurationAccessor.getProviderConfigurationByName(Mockito.any())).thenReturn(Optional.of(configurationModel));
        Mockito.when(providerUserRepository.findByProviderConfigId(Mockito.any())).thenReturn(List.of(providerUserEntity));

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, null, providerUserRepository, configurationAccessor);
        List<ProviderUserModel> providerUserModelList = providerDataAccessor.getUsersByProviderConfigName("providerConfigName-test");

        assertEquals(1, providerUserModelList.size());
        ProviderUserModel providerUserModel = providerUserModelList.get(0);
        assertEquals(projectOwnerEmail, providerUserModel.getEmailAddress());
        assertTrue(providerUserModel.getOptOut());
    }

    @Test
    public void getUsersByProviderConfigNameBlankTest() {
        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(null, null, null, null);
        List<ProviderUserModel> providerUserModelList = providerDataAccessor.getUsersByProviderConfigName("");

        assertTrue(providerUserModelList.isEmpty());
    }

    @Test
    public void getUsersByProviderConfigNameOptionalEmptyTest() throws Exception {
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);

        Mockito.when(configurationAccessor.getProviderConfigurationByName(Mockito.any())).thenReturn(Optional.empty());

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(null, null, null, configurationAccessor);
        List<ProviderUserModel> providerUserModelList = providerDataAccessor.getUsersByProviderConfigName("providerConfigName-test");

        assertTrue(providerUserModelList.isEmpty());
    }

    @Test
    public void updateProjectAndUserDataTest() {
        ProviderProjectEntity storedProviderProjectEntity = new ProviderProjectEntity("stored-name", "stored-description", "stored-href", "stored-Email", 2L);
        ProviderProject providerProject = new ProviderProject(name, description, href, projectOwnerEmail);
        ProviderProjectEntity providerProjectEntity = new ProviderProjectEntity(name, description, href, projectOwnerEmail, 1L);
        providerProjectEntity.setId(3L);
        ProviderUserEntity providerUserEntity = new ProviderUserEntity("test-email", true, 1L);
        providerUserEntity.setId(4L);

        Map<ProviderProject, Set<String>> projectToUserData = new HashMap<>();
        projectToUserData.put(providerProject, Set.of("test-user-email"));
        final String additionalRelevantUsers = "additional-test-user-email";

        ProviderProjectRepository providerProjectRepository = Mockito.mock(ProviderProjectRepository.class);
        ProviderUserRepository providerUserRepository = Mockito.mock(ProviderUserRepository.class);
        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        ProviderUserProjectRelationRepository providerUserProjectRelationRepository = Mockito.mock(ProviderUserProjectRelationRepository.class);

        Mockito.when(providerProjectRepository.findByProviderConfigId(Mockito.any())).thenReturn(List.of(storedProviderProjectEntity));
        Mockito.when(providerProjectRepository.saveAll(Mockito.any())).thenReturn(List.of(providerProjectEntity));
        Mockito.when(providerUserRepository.findByProviderConfigId(Mockito.any())).thenReturn(List.of(providerUserEntity));
        Mockito.when(providerUserRepository.save(Mockito.any())).thenReturn(providerUserEntity);
        Mockito.when(providerProjectRepository.findFirstByHref(Mockito.any())).thenReturn(Optional.of(providerProjectEntity));
        Mockito.when(providerUserRepository.findByEmailAddressAndProviderConfigId(Mockito.any(), Mockito.any())).thenReturn(List.of(providerUserEntity));

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository, configurationAccessor);
        providerDataAccessor.updateProjectAndUserData(1L, projectToUserData, Set.of(additionalRelevantUsers));

        Mockito.verify(providerProjectRepository).deleteByHref(Mockito.any());
        Mockito.verify(providerUserRepository).deleteByProviderConfigIdAndEmailAddress(Mockito.any(), Mockito.any());
        Mockito.verify(providerUserProjectRelationRepository).save(Mockito.any());
    }

    private ConfigurationModel createConfigurationModel() {
        ConfigurationModel configurationModel = new ConfigurationModel(1L, 1L, "createdAt-test", "lastUpdate-test", ConfigContextEnum.DISTRIBUTION);
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(KEY_PROVIDER_CONFIG_NAME);
        configurationFieldModel.setFieldValue(fieldValue);
        configurationModel.put(configurationFieldModel);

        return configurationModel;
    }

    private void testProviderProject(ProviderProject expected, ProviderProject actual) {
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getHref(), actual.getHref());
        assertEquals(expected.getProjectOwnerEmail(), actual.getProjectOwnerEmail());
    }
}
