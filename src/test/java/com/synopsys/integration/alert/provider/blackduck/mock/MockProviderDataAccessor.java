package com.synopsys.integration.alert.provider.blackduck.mock;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.polaris.PolarisProviderKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


import com.synopsys.integration.alert.common.descriptor.DescriptorKey;


public final class MockProviderDataAccessor extends DefaultProviderDataAccessor {
    private final Map<String, Set<ProviderProject>> providerProjectMap;
    private final Set<ProviderUserModel> users;
    private Set<String> expectedEmailAddresses = Set.of();

    public MockProviderDataAccessor() {
        super(null, null, null);
        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        PolarisProviderKey polarisProviderKey = new PolarisProviderKey();
        providerProjectMap = new HashMap<>();
        providerProjectMap.put(blackDuckProviderKey.getUniversalKey(), new HashSet<>());
        providerProjectMap.put(polarisProviderKey.getUniversalKey(), new HashSet<>());
        users = new HashSet<>();
    }

    @Override
    public ProviderProject saveProject(DescriptorKey descriptorKey, ProviderProject providerProject) {
        providerProjectMap.get(descriptorKey.getUniversalKey()).add(providerProject);
        return providerProject;
    }

    @Override
    public void deleteProjects(DescriptorKey descriptorKey, Collection<ProviderProject> providerProjects) {
        Set<ProviderProject> projects = providerProjectMap.get(descriptorKey.getUniversalKey());
        if (null == projects) {
            projects = new HashSet<>();
        }
        projects.removeAll(providerProjects);
        providerProjectMap.put(descriptorKey.getUniversalKey(), projects);
    }

    @Override
    public List<ProviderProject> saveProjects(DescriptorKey descriptorKey, Collection<ProviderProject> providerProjects) {
        Set<ProviderProject> projects = providerProjectMap.get(descriptorKey.getUniversalKey());
        if (null == projects) {
            projects = new HashSet<>();
        }
        projects.addAll(providerProjects);
        providerProjectMap.put(descriptorKey.getUniversalKey(), projects);
        return new ArrayList<>(providerProjects);
    }

    @Override
    public void deleteByHref(String href) {
        providerProjectMap.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(providerProject -> href.equals(providerProject.getHref()))
                .findFirst();
    }

    @Override
    public Set<String> getEmailAddressesForProjectHref(String href) {
        return expectedEmailAddresses;
    }

    @Override
    public void mapUsersToProjectByEmail(String projectHref, Collection<String> emailAddresses) throws AlertDatabaseConstraintException {
        // Implement if needed
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
    public List<ProviderProject> findByProviderKey(DescriptorKey descriptorKey) {
        return new ArrayList<>(providerProjectMap.get(descriptorKey.getUniversalKey()));
    }

    public void setExpectedEmailAddresses(Set<String> expectedEmailAddresses) {
        this.expectedEmailAddresses = expectedEmailAddresses;
    }

    @Override
    public List<ProviderUserModel> getAllUsers(String providerName) {
        return new ArrayList<>(users);
    }

    @Override

    public void deleteUsers(DescriptorKey descriptorKey, Collection<ProviderUserModel> userEntities) {
        for (ProviderUserModel user : userEntities) {
            users.remove(user);
        }
    }

    @Override
    public List<ProviderUserModel> saveUsers(DescriptorKey descriptorKey, Collection<ProviderUserModel> userEntities) {
        for (ProviderUserModel user : userEntities) {
            users.add(user);
        }
        return new ArrayList<>(users);
    }

}
