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

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.datastructure.SetMap;

public final class MockProviderDataAccessor implements ProviderDataAccessor {
    private final Map<Long, String> providerConfigs;
    private final SetMap<Long, ProviderProject> providerProjects;
    private final SetMap<Long, ProviderUserModel> providerUsers;

    private final SetMap<ProviderUserModel, ProviderProject> providerUsersToProjects;
    private Set<String> expectedEmailAddresses = Set.of();

    public MockProviderDataAccessor() {
        providerConfigs = new HashMap<>();
        providerProjects = SetMap.createDefault();
        providerUsers = SetMap.createDefault();
        providerUsersToProjects = SetMap.createDefault();
    }

    public void associateProviderConfigIdWithProviderConfigName(Long id, String name) {
        providerConfigs.put(id, name);
    }

    @Override
    public List<ProviderProject> getProjectsByProviderConfigName(String providerConfigName) {
        return getProviderConfigId(providerConfigName)
                   .map(this::getProjectsByProviderConfigId)
                   .orElse(List.of());
    }

    @Override
    public List<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId) {
        Set<ProviderProject> providerProjectSet = this.providerProjects.get(providerConfigId);
        if (null != providerProjectSet) {
            return new ArrayList<>(providerProjectSet);
        }
        return List.of();
    }

    @Override
    public AlertPagedModel<ProviderProject> getProjectsByProviderConfigId(Long providerConfigId, int pageNumber, int pageSize) {
        Set<ProviderProject> providerProjectSet = this.providerProjects.get(providerConfigId);
        if (null != providerProjectSet) {
            int totalElements = providerProjectSet.size();
            int totalPages = (totalElements + (pageSize - 1)) / pageSize;
            if (totalPages > pageNumber) {
                List<ProviderProject> providerProjectsList = new ArrayList<>(providerProjectSet);

                int offsetStart = pageNumber * pageSize;
                int maxIndex = offsetStart + pageSize;
                int offsetEnd = maxIndex < totalElements ? maxIndex : totalElements - 1;

                providerProjectsList.subList(offsetStart, offsetEnd);
                return new AlertPagedModel<>(totalPages, pageNumber, pageSize, providerProjectsList.subList(offsetStart, offsetStart + pageSize));
            }
        }
        return new AlertPagedModel<>(0, pageNumber, pageSize, List.of());
    }

    @Override
    public List<ProviderUserModel> getUsersByProviderConfigId(Long providerConfigId) {
        Set<ProviderUserModel> providerUserModels = providerUsers.get(providerConfigId);
        if (null != providerUserModels) {
            return new ArrayList<>(providerUserModels);
        }
        return List.of();
    }

    @Override
    public List<ProviderUserModel> getUsersByProviderConfigName(String providerConfigName) {
        return getProviderConfigId(providerConfigName)
                   .map(this::getUsersByProviderConfigId)
                   .orElse(List.of());
    }

    @Override
    public Set<String> getEmailAddressesForProjectHref(Long providerConfigId, String href) {
        // This will just be gotten/set by tests
        return expectedEmailAddresses;
    }

    public void setExpectedEmailAddresses(Set<String> expectedEmailAddresses) {
        this.expectedEmailAddresses = expectedEmailAddresses;
    }

    @Override
    public void deleteProjects(Collection<ProviderProject> providerProjects) {
        for (Set<ProviderProject> currentProjects : this.providerProjects.values()) {
            currentProjects.removeAll(providerProjects);
        }
    }

    @Override
    public void updateProjectAndUserData(Long providerConfigId, Map<ProviderProject, Set<String>> projectToUserData, Set<String> additionalRelevantUsers) {
        updateProjectDB(providerConfigId, projectToUserData.keySet());
        Set<String> userData = projectToUserData.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        userData.addAll(additionalRelevantUsers);
        updateUserDB(providerConfigId, userData);
        updateUserProjectRelations(projectToUserData);
    }

    private List<ProviderProject> updateProjectDB(Long providerConfigId, Set<ProviderProject> currentProjects) {
        Set<ProviderProject> storedProjectsSet = providerProjects.get(providerConfigId);
        if (null == storedProjectsSet) {
            storedProjectsSet = new HashSet<>();
        }

        Set<ProviderProject> projectsToRemove = new HashSet<>(storedProjectsSet);
        projectsToRemove.removeIf(currentProjects::contains);

        Set<ProviderProject> projectsToAdd = new HashSet<>(currentProjects);
        projectsToAdd.removeIf(storedProjectsSet::contains);

        deleteProjects(projectsToRemove);

        this.providerProjects.addAll(providerConfigId, projectsToAdd);
        return new ArrayList<>(this.providerProjects.get(providerConfigId));
    }

    private void updateUserDB(Long providerConfigId, Set<String> userEmailAddresses) {
        Set<String> emailsToAdd = new HashSet<>();
        Set<String> emailsToRemove = new HashSet<>();

        List<ProviderUserModel> providerUserEntities = getUsersByProviderConfigId(providerConfigId);
        Set<String> storedEmails = providerUserEntities
                                       .stream()
                                       .map(ProviderUserModel::getEmailAddress)
                                       .collect(Collectors.toSet());

        for (String storedData : storedEmails) {// If the storedData no longer exists in the current then we need to remove the entry
            // If any of the fields have changed in the currentData, then the storedData will not be in the currentData so we will need to remove the old entry
            if (!userEmailAddresses.contains(storedData)) {
                emailsToRemove.add(storedData);
            }
        }

        for (String currentData : userEmailAddresses) {// If the currentData is not found in the stored data then we will need to add a new entry
            // If any of the fields have changed in the currentData, then it wont be in the stored data so we will need to add a new entry
            if (!storedEmails.contains(currentData)) {
                emailsToAdd.add(currentData);
            }
        }

        Set<ProviderUserModel> providerUserEntitiesToRemove = providerUserEntities
                                                                  .stream()
                                                                  .filter(userEntity -> emailsToRemove.contains(userEntity.getEmailAddress()))
                                                                  .collect(Collectors.toSet());

        Set<ProviderUserModel> providerUserEntitiesToAdd = emailsToAdd
                                                               .stream()
                                                               .map(email -> new ProviderUserModel(email, false))
                                                               .collect(Collectors.toSet());
        providerUsers.computeIfPresent(providerConfigId, (id, users) -> {
            users.removeAll(providerUserEntitiesToRemove);
            return providerUserEntitiesToRemove;
        });
        providerUsers.addAll(providerConfigId, providerUserEntitiesToAdd);
    }

    private void updateUserProjectRelations(Map<ProviderProject, Set<String>> projectToEmailAddresses) {
        for (Map.Entry<ProviderProject, Set<String>> projectToEmail : projectToEmailAddresses.entrySet()) {
            try {
                mapUsersToProjectByEmail(projectToEmail.getKey(), projectToEmail.getValue());
            } catch (AlertDatabaseConstraintException ignored) {
            }
        }
    }

    private void mapUsersToProjectByEmail(ProviderProject project, Collection<String> emailAddresses) throws AlertDatabaseConstraintException {
        Set<ProviderUserModel> applicableUsers = providerUsers
                                                     .values()
                                                     .stream()
                                                     .flatMap(Set::stream)
                                                     .filter(emailAddresses::contains)
                                                     .collect(Collectors.toSet());
        for (ProviderUserModel user : applicableUsers) {
            providerUsersToProjects.add(user, project);
        }
    }

    private Optional<Long> getProviderConfigId(String providerConfigName) {
        return providerConfigs
                   .entrySet()
                   .stream()
                   .filter(entry -> entry.getValue().equals(providerConfigName))
                   .map(Map.Entry::getKey)
                   .findFirst();
    }

}
