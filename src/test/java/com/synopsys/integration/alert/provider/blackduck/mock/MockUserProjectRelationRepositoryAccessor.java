package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.database.api.ProviderUserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.project.ProviderUserProjectRelation;

public class MockUserProjectRelationRepositoryAccessor extends ProviderUserProjectRelationRepositoryAccessor {
    private final Set<ProviderUserProjectRelation> userProjectRelations = new HashSet<>();

    public MockUserProjectRelationRepositoryAccessor() {
        super(null);
    }

    @Override
    public List<ProviderUserProjectRelation> readEntities() {
        return new ArrayList<>(userProjectRelations);
    }

    @Override
    public List<ProviderUserProjectRelation> findByProviderProjectId(final Long providerProjectId) {
        return userProjectRelations.stream().filter(userProjectRelation -> userProjectRelation.getProviderProjectId().equals(providerProjectId)).collect(Collectors.toList());
    }

    @Override
    public List<ProviderUserProjectRelation> findByProviderUserId(final Long providerUserId) {
        return userProjectRelations.stream().filter(userProjectRelation -> userProjectRelation.getProviderUserId().equals(providerUserId)).collect(Collectors.toList());
    }

    @Override
    public List<ProviderUserProjectRelation> deleteAndSaveAll(final Set<ProviderUserProjectRelation> newUserProjectRelations) {
        userProjectRelations.clear();
        userProjectRelations.addAll(newUserProjectRelations);
        return new ArrayList<>(userProjectRelations);
    }

}
