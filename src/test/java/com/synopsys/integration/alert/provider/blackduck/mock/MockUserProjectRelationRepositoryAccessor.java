package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;

public class MockUserProjectRelationRepositoryAccessor extends UserProjectRelationRepositoryAccessor {
    private final Set<UserProjectRelation> userProjectRelations = new HashSet<>();

    public MockUserProjectRelationRepositoryAccessor() {
        super(null);
    }

    @Override
    public List<UserProjectRelation> readEntities() {
        return new ArrayList<>(userProjectRelations);
    }

    @Override
    public List<UserProjectRelation> deleteAndSaveAll(final Set<UserProjectRelation> newUserProjectRelations) {
        userProjectRelations.clear();
        userProjectRelations.addAll(newUserProjectRelations);
        return new ArrayList<>(userProjectRelations);
    }

}
