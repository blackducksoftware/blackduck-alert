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

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
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

    @Test
    public void findFirstByNameTest() {
        final String name = "project name";
        final ProviderProjectEntity expectedEntity = new ProviderProjectEntity(name, null, null, null, null);
        providerProjectRepository.save(expectedEntity);

        final DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        final Optional<ProviderProject> foundProject = providerDataAccessor.findFirstByName(name);
        assertTrue(foundProject.isPresent(), "Expected to find a project");
        assertEquals(name, foundProject.map(ProviderProject::getName).orElse(null));
    }

    @Test
    public void findByProviderNameTest() {
        final String providerName = "provider name";
        final ProviderProjectEntity expectedEntity = new ProviderProjectEntity(null, null, null, null, providerName);
        providerProjectRepository.save(expectedEntity);

        final DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        final List<ProviderProject> foundProjects = providerDataAccessor.findByProviderName(providerName);
        assertEquals(1, foundProjects.size());
    }

    @Test
    public void saveProjectTest() {
        final String name = "name";
        final String description = "description";
        final String href = "hyperlink reference";
        final String projectOwnerEmail = "WhoStillUsesHotmail@hotmail.com";
        final String providerName = "provider name";
        final ProviderProject providerProject = new ProviderProject(name, description, href, projectOwnerEmail);

        final DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        providerDataAccessor.saveProject(providerName, providerProject);

        final List<ProviderProjectEntity> foundProjects = providerProjectRepository.findAll();
        assertEquals(1, foundProjects.size());

        final ProviderProjectEntity foundProject = foundProjects.get(0);
        assertEquals(name, foundProject.getName());
        assertEquals(description, foundProject.getDescription());
        assertEquals(href, foundProject.getHref());
        assertEquals(projectOwnerEmail, foundProject.getProjectOwnerEmail());
        assertEquals(providerName, foundProject.getProvider());
    }

    @Test
    public void deleteAndSaveAllProjectsTest() {
        final String oldProjectHref1 = "href1";
        final String oldProjectHref2 = "href2";
        final String oldProjectHref3 = "href3";
        final String providerName = "provider name";

        final ProviderProjectEntity oldEntity1 = new ProviderProjectEntity(null, null, oldProjectHref1, null, providerName);
        final ProviderProjectEntity oldEntity2 = new ProviderProjectEntity(null, null, oldProjectHref2, null, providerName);
        final ProviderProjectEntity oldEntity3 = new ProviderProjectEntity(null, null, oldProjectHref3, null, providerName);
        providerProjectRepository.save(oldEntity1);
        providerProjectRepository.save(oldEntity2);
        providerProjectRepository.save(oldEntity3);
        List<ProviderProjectEntity> savedEntities = providerProjectRepository.findAll();
        assertEquals(3, savedEntities.size());

        final String newProjectHref1 = "newHref1";
        final String newProjectHref2 = "newHref2";
        final ProviderProject newProject1 = new ProviderProject(null, null, newProjectHref1, null);
        final ProviderProject newProject2 = new ProviderProject(null, null, newProjectHref2, null);
        final List<ProviderProject> newProjects = List.of(newProject1, newProject2);

        final DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);

        final List<ProviderProject> projectsToDelete = savedEntities
                                                           .stream()
                                                           .map(this::convertToProjectModel)
                                                           .collect(Collectors.toList());

        providerDataAccessor.deleteProjects(providerName, projectsToDelete);
        savedEntities = providerProjectRepository.findAll();
        assertEquals(0, savedEntities.size());

        final List<ProviderProject> savedProjects = providerDataAccessor.saveProjects(providerName, newProjects);
        assertEquals(2, savedProjects.size());
        savedEntities = providerProjectRepository.findAll();
        assertEquals(2, savedEntities.size());
    }

    private ProviderProject convertToProjectModel(final ProviderProjectEntity providerProjectEntity) {
        return new ProviderProject(providerProjectEntity.getName(), providerProjectEntity.getDescription(), providerProjectEntity.getHref(),
            providerProjectEntity.getProjectOwnerEmail());
    }

    @Test
    public void deleteByHrefTest() {
        final String oldProjectHref1 = "href1";
        final String oldProjectHref2 = "href2";
        final String oldProjectHref3 = "href3";
        final String providerName = "provider name";

        final ProviderProjectEntity oldEntity1 = new ProviderProjectEntity(null, null, oldProjectHref1, null, providerName);
        final ProviderProjectEntity oldEntity2 = new ProviderProjectEntity(null, null, oldProjectHref2, null, providerName);
        final ProviderProjectEntity oldEntity3 = new ProviderProjectEntity(null, null, oldProjectHref3, null, providerName);
        providerProjectRepository.save(oldEntity1);
        providerProjectRepository.save(oldEntity2);
        providerProjectRepository.save(oldEntity3);
        assertEquals(3, providerProjectRepository.findAll().size());

        final DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        providerDataAccessor.deleteByHref(oldProjectHref2);
        final List<ProviderProjectEntity> foundProjects = providerProjectRepository.findAll();
        assertEquals(2, foundProjects.size());
        assertTrue(foundProjects.stream().noneMatch(project -> oldProjectHref2.equals(project.getHref())), "A project with the deleted href still existed");
    }

    @Test
    public void getEmailAddressesForProjectHrefTest() {
        final String href = "href";
        final String providerName = "provider name";
        final ProviderProjectEntity projectEntityToSave = new ProviderProjectEntity(null, null, href, null, providerName);
        final ProviderProjectEntity savedProjectEntity = providerProjectRepository.save(projectEntityToSave);

        final String emailAddress1 = "someone@gmail.com";
        final String emailAddress2 = "someoneelse@gmail.com";
        final String emailAddress3 = "other@gmail.com";
        final ProviderUserEntity userEntityToSave1 = new ProviderUserEntity(emailAddress1, false, providerName);
        final ProviderUserEntity userEntityToSave2 = new ProviderUserEntity(emailAddress2, false, providerName);
        final ProviderUserEntity userEntityToSave3 = new ProviderUserEntity(emailAddress3, false, providerName);
        final ProviderUserEntity savedUser1 = providerUserRepository.save(userEntityToSave1);
        final ProviderUserEntity savedUser2 = providerUserRepository.save(userEntityToSave2);
        final ProviderUserEntity savedUser3 = providerUserRepository.save(userEntityToSave3);
        // Extra user to test set
        final ProviderUserEntity savedUser4 = providerUserRepository.save(userEntityToSave3);

        final Long projectId = savedProjectEntity.getId();
        providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(savedUser1.getId(), projectId));
        providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(savedUser2.getId(), projectId));
        providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(savedUser3.getId(), projectId));
        providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(savedUser4.getId(), projectId));

        final DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        final Set<String> foundEmailAddresses = providerDataAccessor.getEmailAddressesForProjectHref(href);
        assertEquals(3, foundEmailAddresses.size());
        assertTrue(foundEmailAddresses.contains(emailAddress1), "Expected email address was missing: " + emailAddress1);
        assertTrue(foundEmailAddresses.contains(emailAddress2), "Expected email address was missing: " + emailAddress1);
        assertTrue(foundEmailAddresses.contains(emailAddress3), "Expected email address was missing: " + emailAddress1);
    }

    @Test
    public void getEmailAddressesForNonExistentProjectHrefTest() {
        final DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        final Set<String> foundEmailAddresses = providerDataAccessor.getEmailAddressesForProjectHref("expecting no results");
        assertEquals(0, foundEmailAddresses.size());
    }

    @Test
    public void mapUsersToProjectByEmailTest() throws AlertDatabaseConstraintException {
        final String projectHref = "some value";
        final String providerName = "provider name";
        final ProviderProjectEntity projectToSave = new ProviderProjectEntity(null, null, projectHref, null, providerName);
        final ProviderProjectEntity savedProject = providerProjectRepository.save(projectToSave);

        final String oldEmailAddress = "oldEmail@yahoo.com";
        final ProviderUserEntity existingUser = new ProviderUserEntity(oldEmailAddress, false, providerName);
        final ProviderUserEntity savedExistingUser = providerUserRepository.save(existingUser);

        providerUserProjectRelationRepository.save(new ProviderUserProjectRelation(savedExistingUser.getId(), savedProject.getId()));
        assertEquals(1, providerUserProjectRelationRepository.findAll().size());

        final String newUserEmail1 = "newEmail1@gmail.com";
        final String newUserEmail2 = "newEmail2@gmail.com";
        final String newUserEmail3 = "newEmail3@gmail.com";
        final Set<String> userEmailsToMap = Set.of(newUserEmail1, newUserEmail2, newUserEmail3);
        final ProviderUserEntity newUser1 = new ProviderUserEntity(newUserEmail1, false, providerName);
        final ProviderUserEntity newUser2 = new ProviderUserEntity(newUserEmail2, false, providerName);
        final ProviderUserEntity newUser3 = new ProviderUserEntity(newUserEmail3, false, providerName);
        providerUserRepository.save(newUser1);
        providerUserRepository.save(newUser2);
        providerUserRepository.save(newUser3);

        final DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        providerDataAccessor.remapUsersToProjectByEmail(projectHref, userEmailsToMap);
        assertEquals(3, providerUserProjectRelationRepository.findAll().size());
    }

    @Test
    public void getAllUsersTest() {
        final String providerName = "provider name";
        final String newUserEmail1 = "newEmail1@gmail.com";
        final String newUserEmail2 = "newEmail2@gmail.com";
        final String newUserEmail3 = "newEmail3@gmail.com";
        final ProviderUserEntity newUser1 = new ProviderUserEntity(newUserEmail1, false, providerName);
        final ProviderUserEntity newUser2 = new ProviderUserEntity(newUserEmail2, false, providerName);
        final ProviderUserEntity newUser3 = new ProviderUserEntity(newUserEmail3, false, providerName);
        providerUserRepository.save(newUser1);
        providerUserRepository.save(newUser2);
        providerUserRepository.save(newUser3);

        final DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);
        final List<ProviderUserModel> allProviderUsers = providerDataAccessor.getAllUsers(providerName);
        assertEquals(3, allProviderUsers.size());
    }

    @Test
    public void deleteAndSaveAllUsersTest() {
        final String providerName = "provider name";
        final String newUserEmail1 = "newEmail1@gmail.com";
        final String newUserEmail2 = "newEmail2@gmail.com";
        final String newUserEmail3 = "newEmail3@gmail.com";
        final ProviderUserEntity newUser1 = new ProviderUserEntity(newUserEmail1, false, providerName);
        final ProviderUserEntity newUser2 = new ProviderUserEntity(newUserEmail2, false, providerName);
        final ProviderUserEntity newUser3 = new ProviderUserEntity(newUserEmail3, false, providerName);
        providerUserRepository.save(newUser1);
        providerUserRepository.save(newUser2);
        providerUserRepository.save(newUser3);
        assertEquals(3, providerUserRepository.findAll().size());

        final List<ProviderUserModel> oldUsers = List.of(
            new ProviderUserModel(newUser1.getEmailAddress(), newUser1.getOptOut()),
            new ProviderUserModel(newUser2.getEmailAddress(), newUser2.getOptOut()),
            new ProviderUserModel(newUser3.getEmailAddress(), newUser3.getOptOut())
        );

        final String newUserEmail4 = "newEmail4@gmail.com";
        final String newUserEmail5 = "newEmail5@gmail.com";
        final String newUserEmail6 = "newEmail6@gmail.com";
        final ProviderUserModel newUser4 = new ProviderUserModel(newUserEmail4, false);
        final ProviderUserModel newUser5 = new ProviderUserModel(newUserEmail5, false);
        final ProviderUserModel newUser6 = new ProviderUserModel(newUserEmail6, false);
        final List<ProviderUserModel> newUsers = List.of(newUser4, newUser5, newUser6);

        final DefaultProviderDataAccessor providerDataAccessor = new DefaultProviderDataAccessor(providerProjectRepository, providerUserProjectRelationRepository, providerUserRepository);

        providerDataAccessor.deleteUsers(providerName, oldUsers);
        assertEquals(0, providerUserRepository.findAll().size());

        final List<ProviderUserModel> savedUsers = providerDataAccessor.saveUsers(providerName, newUsers);
        assertEquals(3, savedUsers.size());
        assertEquals(3, providerUserRepository.findAll().size());
    }

}
