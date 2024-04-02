package com.synopsys.integration.alert.channel.jira.server.database.accessor.mock;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.synopsys.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationEntity;
import com.synopsys.integration.alert.channel.jira.server.database.configuration.JiraServerConfigurationRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;
import com.synopsys.integration.alert.test.common.database.MockRepositorySorter;

public class MockJiraServerConfigurationRepository extends MockRepositoryContainer<UUID, JiraServerConfigurationEntity> implements JiraServerConfigurationRepository {
    public MockJiraServerConfigurationRepository(MockRepositorySorter<JiraServerConfigurationEntity> mockRepositorySorter) {
        super(JiraServerConfigurationEntity::getConfigurationId, mockRepositorySorter);
    }

    @Override
    public Optional<JiraServerConfigurationEntity> findByName(String name) {
        Map<UUID, JiraServerConfigurationEntity> dataMap = getDataMap();
        return dataMap.values()
            .stream()
            .filter(entity -> entity.getName().equals(name))
            .findFirst();
    }

    @Override
    public boolean existsByName(String name) {
        Map<UUID, JiraServerConfigurationEntity> dataMap = getDataMap();
        return dataMap.values()
            .stream()
            .anyMatch(entity -> entity.getName().equals(name));
    }

    @Override
    public boolean existsByConfigurationId(UUID uuid) {
        Map<UUID, JiraServerConfigurationEntity> dataMap = getDataMap();
        return dataMap.containsKey(uuid);
    }

    @Override
    public Page<JiraServerConfigurationEntity> findBySearchTerm(String searchTerm, Pageable pageable) {
        Map<UUID, JiraServerConfigurationEntity> dataMap = getDataMap();

        Predicate<JiraServerConfigurationEntity> nameMatches = entity -> entity.getName().contains(searchTerm);

        int pageSize = pageable.getPageSize();
        int pageNumber = pageable.getPageNumber();
        Sort sort = pageable.getSort();

        List<JiraServerConfigurationEntity> sortedAndFilteredEntities = findAll(sort)
            .stream()
            .filter(nameMatches)
            .collect(Collectors.toList());

        List<List<JiraServerConfigurationEntity>> partitions = ListUtils.partition(sortedAndFilteredEntities, pageSize);
        List<JiraServerConfigurationEntity> pageData = List.of();
        for (int currentPage = 0; currentPage < partitions.size(); currentPage++) {
            if (currentPage == pageNumber) {
                pageData = partitions.get(currentPage);
            }
        }
        return new PageImpl<>(pageData, pageable, dataMap.size());
    }
}
