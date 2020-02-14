package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectRepository;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelation;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelationRepository;
import com.synopsys.integration.alert.database.provider.user.ProviderUserEntity;
import com.synopsys.integration.alert.database.provider.user.ProviderUserRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class ProviderDataAccessorTestIT extends AlertIntegrationTest {
    @Autowired
    private ProviderProjectRepository providerProjectRepository;
    @Autowired
    private ProviderUserProjectRelationRepository providerUserProjectRelationRepository;
    @Autowired
    private ProviderUserRepository providerUserRepository;
    @Autowired
    private ConfigurationAccessor configurationAccessor;

    @BeforeEach
    public void init() {
        providerProjectRepository.deleteAllInBatch();
        providerUserProjectRelationRepository.deleteAllInBatch();
        providerUserRepository.deleteAllInBatch();
    }

    private ProviderKey createProviderKey(String key) {
        return new ProviderKey() {
            @Override
            public String getUniversalKey() {
                return key;
            }

            @Override
            public String getDisplayName() {
                return key;
            }
        };
    }

    @Test
    public void deleteAndSaveAllProjectsTest() {
        Long providerConfigId = 10000L;
        String oldProjectHref1 = "href1";
        String oldProjectHref2 = "href2";
        String oldProjectHref3 = "href3";

        ProviderProjectEntity oldEntity1 = new ProviderProjectEntity(null, null, oldProjectHref1, null, providerConfigId);
        ProviderProjectEntity oldEntity2 = new ProviderProjectEntity(null, null, oldProjectHref2, null, providerConfigId);
        ProviderProjectEntity oldEntity3 = new ProviderProjectEntity(null, null, oldProjectHref3, null, providerConfigId);
        providerProjectRepository.save(oldEntity1);
        providerProjectRepository.save(oldEntity2);
        providerProjectRepository.save(oldEntity3);
        List<ProviderProjectEntity> savedEntities = providerProjectRepository.findAll();
        assertEquals(3, savedEntities.size());

        String newProjectHref1 = "newHref1";
        String newProjectHref2 = "newHref2";
        ProviderProject newProject1 = new ProviderProject(null, null, newProjectHref1, null);
        ProviderProject newProject2 = new ProviderProject(null, null, newProjectHref2, null);
        List<ProviderProject> newProjects = List.of(newProject1, newProject2);

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository, configurationAccessor);

        List<ProviderProject> projectsToDelete = savedEntities
                                                     .stream()
                                                     .map(this::convertToProjectModel)
                                                     .collect(Collectors.toList());

        providerDataAccessor.deleteProjects(projectsToDelete);
        savedEntities = providerProjectRepository.findAll();
        assertEquals(0, savedEntities.size());

        List<ProviderProject> savedProjects = saveProjects(providerConfigId, newProjects);

        assertEquals(2, savedProjects.size());
        savedEntities = providerProjectRepository.findAll();
        assertEquals(2, savedEntities.size());
    }

    @Test
    public void getEmailAddressesForProjectHrefTest() {
        Long providerConfigId = 10000L;
        String href = "href";
        ProviderProjectEntity projectEntityToSave = new ProviderProjectEntity(null, null, href, null, providerConfigId);
        ProviderProjectEntity savedProjectEntity = providerProjectRepository.save(projectEntityToSave);

        String emailAddress1 = "someone@gmail.com";
        String emailAddress2 = "someoneelse@gmail.com";
        String emailAddress3 = "other@gmail.com";
        ProviderUserEntity userEntityToSave1 = new ProviderUserEntity(emailAddress1, false, providerConfigId);
        ProviderUserEntity userEntityToSave2 = new ProviderUserEntity(emailAddress2, false, providerConfigId);
        ProviderUserEntity userEntityToSave3 = new ProviderUserEntity(emailAddress3, false, providerConfigId);
        ProviderUserEntity savedUser1 = providerUserRepository.save(userEntityToSave1);
        ProviderUserEntity savedUser2 = providerUserRepository.save(userEntityToSave2);
        ProviderUserEntity savedUser3 = providerUserRepository.save(userEntityToSave3);
        // Extra user to test set
        ProviderUserEntity savedUser4 = providerUserRepository.save(userEntityToSave3);

        Long projectId = savedProjectEntity.getId();
        providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(savedUser1.getId(), projectId));
        providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(savedUser2.getId(), projectId));
        providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(savedUser3.getId(), projectId));
        providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(savedUser4.getId(), projectId));

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository, configurationAccessor);
        Set<String> foundEmailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(href);
        assertEquals(3, foundEmailAddresses.size());
        assertTrue(foundEmailAddresses.contains(emailAddress1), "Expected email address was missing: " + emailAddress1);
        assertTrue(foundEmailAddresses.contains(emailAddress2), "Expected email address was missing: " + emailAddress1);
        assertTrue(foundEmailAddresses.contains(emailAddress3), "Expected email address was missing: " + emailAddress1);
    }

    @Test
    public void getEmailAddressesForNonExistentProjectHrefTest() {
        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository, configurationAccessor);
        Set<String> foundEmailAddresses = providerDataAccessor.getEmailAddressesForProjectHref("expecting no results");
        assertEquals(0, foundEmailAddresses.size());
    }

    @Test
    public void getAllUsersTest() {
        Long providerConfigId = 10000L;
        String newUserEmail1 = "newEmail1@gmail.com";
        String newUserEmail2 = "newEmail2@gmail.com";
        String newUserEmail3 = "newEmail3@gmail.com";
        ProviderUserEntity newUser1 = new ProviderUserEntity(newUserEmail1, false, providerConfigId);
        ProviderUserEntity newUser2 = new ProviderUserEntity(newUserEmail2, false, providerConfigId);
        ProviderUserEntity newUser3 = new ProviderUserEntity(newUserEmail3, false, providerConfigId);
        providerUserRepository.save(newUser1);
        providerUserRepository.save(newUser2);
        providerUserRepository.save(newUser3);

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository, configurationAccessor);
        List<ProviderUserModel> allProviderUsers = providerDataAccessor.getAllUsers(providerConfigId);
        assertEquals(3, allProviderUsers.size());
    }

    @Test
    public void deleteAndSaveAllUsersTest() {
        Long providerConfigId = 10000L;
        String newUserEmail1 = "newEmail1@gmail.com";
        String newUserEmail2 = "newEmail2@gmail.com";
        String newUserEmail3 = "newEmail3@gmail.com";
        ProviderUserEntity newUser1 = new ProviderUserEntity(newUserEmail1, false, providerConfigId);
        ProviderUserEntity newUser2 = new ProviderUserEntity(newUserEmail2, false, providerConfigId);
        ProviderUserEntity newUser3 = new ProviderUserEntity(newUserEmail3, false, providerConfigId);
        providerUserRepository.save(newUser1);
        providerUserRepository.save(newUser2);
        providerUserRepository.save(newUser3);
        assertEquals(3, providerUserRepository.findAll().size());

        List<ProviderUserModel> oldUsers = List.of(
            new ProviderUserModel(newUser1.getEmailAddress(), newUser1.getOptOut()),
            new ProviderUserModel(newUser2.getEmailAddress(), newUser2.getOptOut()),
            new ProviderUserModel(newUser3.getEmailAddress(), newUser3.getOptOut())
        );

        String newUserEmail4 = "newEmail4@gmail.com";
        String newUserEmail5 = "newEmail5@gmail.com";
        String newUserEmail6 = "newEmail6@gmail.com";
        ProviderUserModel newUser4 = new ProviderUserModel(newUserEmail4, false);
        ProviderUserModel newUser5 = new ProviderUserModel(newUserEmail5, false);
        ProviderUserModel newUser6 = new ProviderUserModel(newUserEmail6, false);
        List<ProviderUserModel> newUsers = List.of(newUser4, newUser5, newUser6);

        deleteUsers(providerConfigId, oldUsers);
        assertEquals(0, providerUserRepository.findAll().size());

        List<ProviderUserModel> savedUsers = saveUsers(providerConfigId, newUsers);

        assertEquals(3, savedUsers.size());
        assertEquals(3, providerUserRepository.findAll().size());
    }

    private List<ProviderUserModel> saveUsers(Long providerConfigId, Collection<ProviderUserModel> users) {
        return users
                   .stream()
                   .map(user -> convertToUserEntity(providerConfigId, user))
                   .map(providerUserRepository::save)
                   .map(this::convertToUserModel)
                   .collect(Collectors.toList());
    }

    private void deleteUsers(Long providerConfigId, Collection<ProviderUserModel> users) {
        users.forEach(user -> providerUserRepository.deleteByProviderConfigIdAndEmailAddress(providerConfigId, user.getEmailAddress()));
    }

    private List<ProviderProject> saveProjects(Long providerConfigId, Collection<ProviderProject> providerProjects) {
        Iterable<ProviderProjectEntity> providerProjectEntities = providerProjects
                                                                      .stream()
                                                                      .map(project -> convertToProjectEntity(providerConfigId, project))
                                                                      .collect(Collectors.toSet());
        List<ProviderProjectEntity> savedEntities = providerProjectRepository.saveAll(providerProjectEntities);
        return savedEntities
                   .stream()
                   .map(this::convertToProjectModel)
                   .collect(Collectors.toList());
    }

    private ProviderProject convertToProjectModel(ProviderProjectEntity providerProjectEntity) {
        return new ProviderProject(providerProjectEntity.getName(), providerProjectEntity.getDescription(), providerProjectEntity.getHref(),
            providerProjectEntity.getProjectOwnerEmail());
    }

    private ProviderProjectEntity convertToProjectEntity(Long providerConfigId, ProviderProject providerProject) {
        String trimmedDescription = StringUtils.abbreviate(providerProject.getDescription(), DefaultProviderDataAccessor.MAX_DESCRIPTION_LENGTH);
        return new ProviderProjectEntity(providerProject.getName(), trimmedDescription, providerProject.getHref(), providerProject.getProjectOwnerEmail(), providerConfigId);
    }

    private ProviderUserModel convertToUserModel(ProviderUserEntity providerUserEntity) {
        return new ProviderUserModel(providerUserEntity.getEmailAddress(), providerUserEntity.getOptOut());
    }

    private ProviderUserEntity convertToUserEntity(Long providerConfigId, ProviderUserModel providerUserModel) {
        return new ProviderUserEntity(providerUserModel.getEmailAddress(), providerUserModel.getOptOut(), providerConfigId);
    }

}
