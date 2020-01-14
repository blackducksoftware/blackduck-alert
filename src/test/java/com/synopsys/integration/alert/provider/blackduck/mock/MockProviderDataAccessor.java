package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.database.provider.project.ProviderProjectEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;

public final class MockProviderDataAccessor extends DefaultProviderDataAccessor {
    private final Map<String, Set<ProviderProject>> providerProjectMap;
    private final Set<ProviderUserModel> users;
    private Set<String> expectedEmailAddresses = Set.of();

    public MockProviderDataAccessor() {
        super(null, null, null);
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        providerProjectMap = new HashMap<>();
        providerProjectMap.put(blackDuckProviderKey.getUniversalKey(), new HashSet<>());
        users = new HashSet<>();
    }

    @Override
    public void updateProjectAndUserData(ProviderKey providerKey, Map<ProviderProject, Set<String>> projectToUserData, Set<String> additionalRelevantUsers) {
        updateProjectDB(providerKey, projectToUserData.keySet());
        Set<String> userData = projectToUserData.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        userData.addAll(additionalRelevantUsers);
        updateUserDB(providerKey, userData);
        updateUserProjectRelations(projectToUserData);
    }

    @Override
    public void deleteProjects(ProviderKey descriptorKey, Collection<ProviderProject> providerProjects) {
        Set<ProviderProject> projects = providerProjectMap.get(descriptorKey.getUniversalKey());
        if (null == projects) {
            projects = new HashSet<>();
        }
        projects.removeAll(providerProjects);
        providerProjectMap.put(descriptorKey.getUniversalKey(), projects);
    }

    @Override
    public Set<String> getEmailAddressesForProjectHref(String href) {
        return expectedEmailAddresses;
    }

    @Override
    public Optional<ProviderProject> findFirstByName(String name) {
        return providerProjectMap.values()
                   .stream()
                   .flatMap(Collection::stream)
                   .filter(providerProject -> name.equals(providerProject.getName()))
                   .findFirst();
    }

    @Override
    public List<ProviderProject> findByProviderName(String providerName) {
        return new ArrayList<>(providerProjectMap.get(providerName));
    }

    @Override
    public List<ProviderProject> findByProviderKey(ProviderKey descriptorKey) {
        return new ArrayList<>(providerProjectMap.get(descriptorKey.getUniversalKey()));
    }

    public void setExpectedEmailAddresses(Set<String> expectedEmailAddresses) {
        this.expectedEmailAddresses = expectedEmailAddresses;
    }

    @Override
    public List<ProviderUserModel> getAllUsers(String providerName) {
        return new ArrayList<>(users);
    }

    private List<ProviderProject> updateProjectDB(ProviderKey providerKey, Set<ProviderProject> currentProjects) {
        Set<ProviderProject> projectsToAdd = new HashSet<>();
        Set<ProviderProject> projectsToRemove = new HashSet<>();
        List<ProviderProject> storedProjects = findByProviderKey(providerKey);

        projectsToRemove.addAll(storedProjects);
        projectsToRemove.removeIf(currentProjects::contains);

        projectsToAdd.addAll(currentProjects);
        projectsToAdd.removeIf(storedProjects::contains);

        deleteProjects(providerKey, projectsToRemove);

        Set<ProviderProject> providerProjects = providerProjectMap.get(providerKey.getUniversalKey());
        providerProjects.addAll(projectsToAdd);
        return new ArrayList<>(providerProjects);
    }

    private void updateUserDB(ProviderKey providerKey, Set<String> userEmailAddresses) {
        Set<String> emailsToAdd = new HashSet<>();
        Set<String> emailsToRemove = new HashSet<>();

        List<ProviderUserModel> providerUserEntities = getAllUsers(providerKey.getUniversalKey());
        Set<String> storedEmails = providerUserEntities
                                       .stream()
                                       .map(ProviderUserModel::getEmailAddress)
                                       .collect(Collectors.toSet());

        storedEmails.forEach(storedData -> {
            // If the storedData no longer exists in the current then we need to remove the entry
            // If any of the fields have changed in the currentData, then the storedData will not be in the currentData so we will need to remove the old entry
            if (!userEmailAddresses.contains(storedData)) {
                emailsToRemove.add(storedData);
            }
        });
        userEmailAddresses.forEach(currentData -> {
            // If the currentData is not found in the stored data then we will need to add a new entry
            // If any of the fields have changed in the currentData, then it wont be in the stored data so we will need to add a new entry
            if (!storedEmails.contains(currentData)) {
                emailsToAdd.add(currentData);
            }
        });

        List<ProviderUserModel> providerUserEntitiesToRemove = providerUserEntities
                                                                   .stream()
                                                                   .filter(userEntity -> emailsToRemove.contains(userEntity.getEmailAddress()))
                                                                   .collect(Collectors.toList());

        List<ProviderUserModel> providerUserEntitiesToAdd = emailsToAdd
                                                                .stream()
                                                                .map(email -> new ProviderUserModel(email, false))
                                                                .collect(Collectors.toList());
        users.removeAll(providerUserEntitiesToRemove);
        users.addAll(providerUserEntitiesToAdd);
    }

    private void updateUserProjectRelations(Map<ProviderProject, Set<String>> projectToEmailAddresses) {
        for (Map.Entry<ProviderProject, Set<String>> projectToEmail : projectToEmailAddresses.entrySet()) {
            try {
                mapUsersToProjectByEmail(projectToEmail.getKey().getHref(), projectToEmail.getValue());
            } catch (AlertDatabaseConstraintException e) {
                Assertions.fail(e.getMessage());
            }
        }
    }

    private void mapUsersToProjectByEmail(String projectHref, Collection<String> emailAddresses) throws AlertDatabaseConstraintException {
        // Do nothing
    }

    private ProviderProjectEntity convertToProjectEntity(ProviderKey providerKey, ProviderProject providerProject) {
        String trimmedDescription = StringUtils.abbreviate(providerProject.getDescription(), MAX_DESCRIPTION_LENGTH);
        return new ProviderProjectEntity(providerProject.getName(), trimmedDescription, providerProject.getHref(), providerProject.getProjectOwnerEmail(), providerKey.getUniversalKey());
    }

}
