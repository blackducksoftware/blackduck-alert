package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.database.api.BlackDuckUserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.BlackDuckUserProjectRelation;

public class MockUserProjectRelationRepositoryAccessor extends BlackDuckUserProjectRelationRepositoryAccessor {
    private final Set<BlackDuckUserProjectRelation> userProjectRelations = new HashSet<>();

    public MockUserProjectRelationRepositoryAccessor() {
        super(null);
    }

    @Override
    public List<BlackDuckUserProjectRelation> readEntities() {
        return new ArrayList<>(userProjectRelations);
    }

    @Override
    public List<BlackDuckUserProjectRelation> findByBlackDuckProjectId(final Long blackDuckProjectId) {
        return userProjectRelations.stream().filter(userProjectRelation -> userProjectRelation.getBlackDuckProjectId().equals(blackDuckProjectId)).collect(Collectors.toList());
    }

    @Override
    public List<BlackDuckUserProjectRelation> findByBlackDuckUserId(final Long blackDuckUserId) {
        return userProjectRelations.stream().filter(userProjectRelation -> userProjectRelation.getBlackDuckUserId().equals(blackDuckUserId)).collect(Collectors.toList());
    }

    @Override
    public List<BlackDuckUserProjectRelation> deleteAndSaveAll(final Set<BlackDuckUserProjectRelation> newUserProjectRelations) {
        userProjectRelations.clear();
        userProjectRelations.addAll(newUserProjectRelations);
        return new ArrayList<>(userProjectRelations);
    }

}
