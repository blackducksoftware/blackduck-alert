package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
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

    @BeforeEach
    public void init() {
        providerProjectRepository.deleteAllInBatch();
        providerUserProjectRelationRepository.deleteAllInBatch();
        providerUserRepository.deleteAllInBatch();
    }

    private DescriptorKey createDescriptorKey(String key) {
        DescriptorKey testDescriptorKey = new DescriptorKey() {
            @Override
            public String getUniversalKey() {
                return key;
            }

            @Override
            public String getDisplayName() {
                return key;
            }
        };
        return testDescriptorKey;
    }

    @Test
    public void findFirstByNameTest() {
        String name = "project name";
        ProviderProjectEntity expectedEntity = new ProviderProjectEntity(name, null, null, null, null);
        providerProjectRepository.save(expectedEntity);

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        Optional<ProviderProject> foundProject = providerDataAccessor.findFirstByName(name);
        assertTrue(foundProject.isPresent(), "Expected to find a project");
        assertEquals(name, foundProject.map(ProviderProject::getName).orElse(null));
    }

    @Test
    public void findByProviderNameTest() {
        String providerName = "provider name";
        ProviderProjectEntity expectedEntity = new ProviderProjectEntity(null, null, null, null, providerName);
        providerProjectRepository.save(expectedEntity);

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        List<ProviderProject> foundProjects = providerDataAccessor.findByProviderName(providerName);
        assertEquals(1, foundProjects.size());
    }

    @Test
    public void saveProjectTest() {

        String name = "name";
        String description = "description";
        String href = "hyperlink reference";
        String projectOwnerEmail = "WhoStillUsesHotmail@hotmail.com";
        String providerName = "provider name";
        ProviderProject providerProject = new ProviderProject(name, description, href, projectOwnerEmail);

        DescriptorKey descriptorKey = createDescriptorKey(providerName);

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        providerDataAccessor.saveProject(descriptorKey, providerProject);

        List<ProviderProjectEntity> foundProjects = providerProjectRepository.findAll();
        assertEquals(1, foundProjects.size());

        ProviderProjectEntity foundProject = foundProjects.get(0);
        assertEquals(name, foundProject.getName());
        assertEquals(description, foundProject.getDescription());
        assertEquals(href, foundProject.getHref());
        assertEquals(projectOwnerEmail, foundProject.getProjectOwnerEmail());
        assertEquals(providerName, foundProject.getProvider());
    }

    @Test
    public void deleteAndSaveAllProjectsTest() {
        String oldProjectHref1 = "href1";
        String oldProjectHref2 = "href2";
        String oldProjectHref3 = "href3";
        String providerName = "provider name";

        ProviderProjectEntity oldEntity1 = new ProviderProjectEntity(null, null, oldProjectHref1, null, providerName);
        ProviderProjectEntity oldEntity2 = new ProviderProjectEntity(null, null, oldProjectHref2, null, providerName);
        ProviderProjectEntity oldEntity3 = new ProviderProjectEntity(null, null, oldProjectHref3, null, providerName);
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

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);

        List<ProviderProject> projectsToDelete = savedEntities
                                                     .stream()
                                                     .map(this::convertToProjectModel)
                                                     .collect(Collectors.toList());
        DescriptorKey descriptorKey = createDescriptorKey(providerName);

        providerDataAccessor.deleteProjects(descriptorKey, projectsToDelete);
        savedEntities = providerProjectRepository.findAll();
        assertEquals(0, savedEntities.size());

        List<ProviderProject> savedProjects = providerDataAccessor.saveProjects(descriptorKey, newProjects);

        assertEquals(2, savedProjects.size());
        savedEntities = providerProjectRepository.findAll();
        assertEquals(2, savedEntities.size());
    }

    private ProviderProject convertToProjectModel(ProviderProjectEntity providerProjectEntity) {
        return new ProviderProject(providerProjectEntity.getName(), providerProjectEntity.getDescription(), providerProjectEntity.getHref(),
            providerProjectEntity.getProjectOwnerEmail());
    }

    @Test
    public void deleteByHrefTest() {
        String oldProjectHref1 = "href1";
        String oldProjectHref2 = "href2";
        String oldProjectHref3 = "href3";
        String providerName = "provider name";

        ProviderProjectEntity oldEntity1 = new ProviderProjectEntity(null, null, oldProjectHref1, null, providerName);
        ProviderProjectEntity oldEntity2 = new ProviderProjectEntity(null, null, oldProjectHref2, null, providerName);
        ProviderProjectEntity oldEntity3 = new ProviderProjectEntity(null, null, oldProjectHref3, null, providerName);
        providerProjectRepository.save(oldEntity1);
        providerProjectRepository.save(oldEntity2);
        providerProjectRepository.save(oldEntity3);
        assertEquals(3, providerProjectRepository.findAll().size());

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        providerDataAccessor.deleteByHref(oldProjectHref2);
        List<ProviderProjectEntity> foundProjects = providerProjectRepository.findAll();
        assertEquals(2, foundProjects.size());
        assertTrue(foundProjects.stream().noneMatch(project -> oldProjectHref2.equals(project.getHref())), "A project with the deleted href still existed");
    }

    @Test
    public void getEmailAddressesForProjectHrefTest() {
        String href = "href";
        String providerName = "provider name";
        ProviderProjectEntity projectEntityToSave = new ProviderProjectEntity(null, null, href, null, providerName);
        ProviderProjectEntity savedProjectEntity = providerProjectRepository.save(projectEntityToSave);

        String emailAddress1 = "someone@gmail.com";
        String emailAddress2 = "someoneelse@gmail.com";
        String emailAddress3 = "other@gmail.com";
        ProviderUserEntity userEntityToSave1 = new ProviderUserEntity(emailAddress1, false, providerName);
        ProviderUserEntity userEntityToSave2 = new ProviderUserEntity(emailAddress2, false, providerName);
        ProviderUserEntity userEntityToSave3 = new ProviderUserEntity(emailAddress3, false, providerName);
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

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        Set<String> foundEmailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(href);
        assertEquals(3, foundEmailAddresses.size());
        assertTrue(foundEmailAddresses.contains(emailAddress1), "Expected email address was missing: " + emailAddress1);
        assertTrue(foundEmailAddresses.contains(emailAddress2), "Expected email address was missing: " + emailAddress1);
        assertTrue(foundEmailAddresses.contains(emailAddress3), "Expected email address was missing: " + emailAddress1);
    }

    @Test
    public void getEmailAddressesForNonExistentProjectHrefTest() {
        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        Set<String> foundEmailAddresses = providerDataAccessor.getEmailAddressesForProjectHref("expecting no results");
        assertEquals(0, foundEmailAddresses.size());
    }

    @Test
    public void mapUsersToProjectByEmailTest() throws AlertDatabaseConstraintException {
        String projectHref = "some value";
        String providerName = "provider name";
        ProviderProjectEntity projectToSave = new ProviderProjectEntity(null, null, projectHref, null, providerName);
        ProviderProjectEntity savedProject = providerProjectRepository.save(projectToSave);

        String oldEmailAddress = "oldEmail@yahoo.com";
        ProviderUserEntity existingUser = new ProviderUserEntity(oldEmailAddress, false, providerName);
        ProviderUserEntity savedExistingUser = providerUserRepository.save(existingUser);

        providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(savedExistingUser.getId(), savedProject.getId()));
        assertEquals(1, providerUserProjectRelationRepository.findAll().size());
        providerUserProjectRelationRepository.deleteAllInBatch();

        String newUserEmail1 = "newEmail1@gmail.com";
        String newUserEmail2 = "newEmail2@gmail.com";
        String newUserEmail3 = "newEmail3@gmail.com";
        Set<String> userEmailsToMap = Set.of(newUserEmail1, newUserEmail2, newUserEmail3);
        ProviderUserEntity newUser1 = new ProviderUserEntity(newUserEmail1, false, providerName);
        ProviderUserEntity newUser2 = new ProviderUserEntity(newUserEmail2, false, providerName);
        ProviderUserEntity newUser3 = new ProviderUserEntity(newUserEmail3, false, providerName);
        providerUserRepository.save(newUser1);
        providerUserRepository.save(newUser2);
        providerUserRepository.save(newUser3);

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        providerDataAccessor.mapUsersToProjectByEmail(projectHref, userEmailsToMap);
        assertEquals(3, providerUserProjectRelationRepository.findAll().size());
    }

    @Test
    public void getAllUsersTest() {
        String providerName = "provider name";
        String newUserEmail1 = "newEmail1@gmail.com";
        String newUserEmail2 = "newEmail2@gmail.com";
        String newUserEmail3 = "newEmail3@gmail.com";
        ProviderUserEntity newUser1 = new ProviderUserEntity(newUserEmail1, false, providerName);
        ProviderUserEntity newUser2 = new ProviderUserEntity(newUserEmail2, false, providerName);
        ProviderUserEntity newUser3 = new ProviderUserEntity(newUserEmail3, false, providerName);
        providerUserRepository.save(newUser1);
        providerUserRepository.save(newUser2);
        providerUserRepository.save(newUser3);

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        List<ProviderUserModel> allProviderUsers = providerDataAccessor.getAllUsers(providerName);
        assertEquals(3, allProviderUsers.size());
    }

    @Test
    public void getPageOfUsersTest() throws AlertDatabaseConstraintException {
        String providerName = "provider name";
        String newUserEmail1 = "newEmail1@gmail.com";
        String newUserEmail2 = "newEmail2@gmail.com";
        String newUserEmail3 = "newEmail3@gmail.com";
        String newUserEmail4 = "newEmail4@gmail.com";
        ProviderUserEntity newUser1 = new ProviderUserEntity(newUserEmail1, false, providerName);
        ProviderUserEntity newUser2 = new ProviderUserEntity(newUserEmail2, false, providerName);
        ProviderUserEntity newUser3 = new ProviderUserEntity(newUserEmail3, false, providerName);
        ProviderUserEntity newUser4 = new ProviderUserEntity(newUserEmail4, false, providerName);
        providerUserRepository.save(newUser1);
        providerUserRepository.save(newUser2);
        providerUserRepository.save(newUser3);
        providerUserRepository.save(newUser4);

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);

        // Test pageNumber
        AlertPagedModel<ProviderUserModel> offsetUsers = providerDataAccessor.getPageOfUsers(providerName, 1, 2, null);
        assertEquals(2, offsetUsers.getContent().size());

        // Test pageSize
        AlertPagedModel<ProviderUserModel> limitedUsers = providerDataAccessor.getPageOfUsers(providerName, 0, 2, null);
        assertEquals(2, limitedUsers.getContent().size());

        // Test q
        AlertPagedModel<ProviderUserModel> filteredUsers = providerDataAccessor.getPageOfUsers(providerName, 0, 100, "3");
        assertEquals(1, filteredUsers.getContent().size());
    }

    @Test
    public void deleteAndSaveAllUsersTest() {
        String providerName = "provider name";
        String newUserEmail1 = "newEmail1@gmail.com";
        String newUserEmail2 = "newEmail2@gmail.com";
        String newUserEmail3 = "newEmail3@gmail.com";
        ProviderUserEntity newUser1 = new ProviderUserEntity(newUserEmail1, false, providerName);
        ProviderUserEntity newUser2 = new ProviderUserEntity(newUserEmail2, false, providerName);
        ProviderUserEntity newUser3 = new ProviderUserEntity(newUserEmail3, false, providerName);
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

        DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);

        DescriptorKey descriptorKey = createDescriptorKey(providerName);

        providerDataAccessor.deleteUsers(descriptorKey, oldUsers);
        assertEquals(0, providerUserRepository.findAll().size());

        List<ProviderUserModel> savedUsers = providerDataAccessor.saveUsers(descriptorKey, newUsers);

        assertEquals(3, savedUsers.size());
        assertEquals(3, providerUserRepository.findAll().size());
    }

}
