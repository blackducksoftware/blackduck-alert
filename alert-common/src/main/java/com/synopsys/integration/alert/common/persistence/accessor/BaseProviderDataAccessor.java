package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;

public interface BaseProviderDataAccessor {
    Optional<ProviderProject> findFirstByName(final String name);

    List<ProviderProject> findByProviderName(final String providerName);

    ProviderProject saveProject(final String providerName, final ProviderProject providerProject);

    List<ProviderProject> deleteAndSaveAllProjects(final String providerName, final Collection<ProviderProject> providerProjects);

    void deleteByHref(final String projectHref);

    Set<String> getEmailAddressesForProjectHref(final String projectHref);

    void remapUsersToProjectByEmail(final String projectHref, final Collection<String> emailAddresses) throws AlertDatabaseConstraintException;

    List<ProviderUserModel> getAllUsers(final String providerName);

    List<ProviderUserModel> deleteAndSaveAllUsers(final String providerName, final Collection<ProviderUserModel> usersToDelete, final Collection<ProviderUserModel> usersToAdd);
}
