/*
 * alert-database-job
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.api;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.persistence.accessor.DistributionAccessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.DistributionWithAuditInfo;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.distribution.DistributionRepository;
import com.synopsys.integration.alert.database.distribution.DistributionWithAuditEntity;

@Component
public class DefaultDistributionAccessor implements DistributionAccessor {
    private final Logger logger = LoggerFactory.getLogger(DefaultDistributionAccessor.class);
    private final DistributionRepository distributionRepository;

    @Autowired
    public DefaultDistributionAccessor(DistributionRepository distributionRepository) {
        this.distributionRepository = distributionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfo(int page, int pageSize, String sortName, Direction sortOrder, Set<String> allowedDescriptorNames) {
        return retrieveData(page, pageSize, sortName, sortOrder, (pageRequest -> distributionRepository.getDistributionWithAuditInfo(pageRequest, allowedDescriptorNames)));
    }

    @Override
    @Transactional(readOnly = true)
    public AlertPagedModel<DistributionWithAuditInfo> getDistributionWithAuditInfoWithSearch(int page, int pageSize, String sortName, Direction sortOrder, Set<String> allowedDescriptorNames, String searchTerm) {
        return retrieveData(page, pageSize, sortName, sortOrder, (pageRequest -> distributionRepository.getDistributionWithAuditInfoWithSearch(pageRequest, allowedDescriptorNames, searchTerm)));
    }

    private AlertPagedModel<DistributionWithAuditInfo> retrieveData(int page, int pageSize, String sortName, Direction sortOrder, Function<PageRequest, Page<DistributionWithAuditEntity>> retrieveData) {
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(sortOrder, sortName));
        Page<DistributionWithAuditEntity> distributionWithAuditInfo = retrieveData.apply(pageRequest);
        return convert(distributionWithAuditInfo);
    }

    private AlertPagedModel<DistributionWithAuditInfo> convert(Page<DistributionWithAuditEntity> pageOfDistributionWithAuditEntity) {
        List<DistributionWithAuditInfo> results = dedupeList(pageOfDistributionWithAuditEntity.getContent());
        return new AlertPagedModel<>(
            pageOfDistributionWithAuditEntity.getTotalPages(),
            pageOfDistributionWithAuditEntity.getNumber(),
            pageOfDistributionWithAuditEntity.getSize(),
            results
        );
    }

    // FIXME Bug, we need to modify the query to successfully avoid duplicate jobs.
    private List<DistributionWithAuditInfo> dedupeList(List<DistributionWithAuditEntity> withAuditEntities) {
        Map<UUID, DistributionWithAuditInfo> cachedInfo = new HashMap<>();
        withAuditEntities.stream().forEach(entity -> cacheMaxValues(cachedInfo, entity));
        return filterList(cachedInfo, withAuditEntities);
    }

    private void cacheMaxValues(Map<UUID, DistributionWithAuditInfo> existingInfos, DistributionWithAuditEntity entity) {
        UUID jobId = entity.getJobId();
        DistributionWithAuditInfo existingEntity = existingInfos.get(jobId);
        if (null == existingEntity) {
            existingInfos.put(jobId, convert(entity));
            return;
        }

        String currentTimeLastSent = existingEntity.getAuditTimeLastSent();
        if (null == currentTimeLastSent) {
            existingInfos.put(jobId, convert(entity));
            return;
        }

        try {
            OffsetDateTime storedDateTime = DateUtils.parseDate(currentTimeLastSent, DateUtils.AUDIT_DATE_FORMAT);
            if (entity.getAuditTimeLastSent().compareTo(storedDateTime) > 0) {
                existingInfos.put(jobId, convert(entity));
            }
        } catch (ParseException e) {
            logger.error("Unexpected error when parsing date: {}", e.getMessage());
        }
    }

    private List<DistributionWithAuditInfo> filterList(Map<UUID, DistributionWithAuditInfo> cachedInfo, List<DistributionWithAuditEntity> existingInfo) {
        ArrayList<DistributionWithAuditInfo> distributionsWithAuditInfos = new ArrayList<>(cachedInfo.entrySet().size());
        existingInfo.stream().forEach(info -> {
            UUID jobId = info.getJobId();
            DistributionWithAuditInfo distributionWithAuditInfo = cachedInfo.get(jobId);
            DistributionWithAuditInfo originalInfo = convert(info);
            if (originalInfo.equals(distributionWithAuditInfo)) {
                distributionsWithAuditInfos.add(distributionWithAuditInfo);
                cachedInfo.remove(jobId);
            }
        });
        return distributionsWithAuditInfos;
    }

    private DistributionWithAuditInfo convert(DistributionWithAuditEntity distributionWithAuditEntity) {
        return new DistributionWithAuditInfo(
            distributionWithAuditEntity.getJobId(),
            distributionWithAuditEntity.isEnabled(),
            distributionWithAuditEntity.getJobName(),
            distributionWithAuditEntity.getChannelName(),
            FrequencyType.valueOf(distributionWithAuditEntity.getFrequencyType()),
            formatAuditDate(distributionWithAuditEntity.getAuditTimeLastSent()),
            distributionWithAuditEntity.getAuditStatus()
        );
    }

    private String formatAuditDate(OffsetDateTime dateTime) {
        if (null != dateTime) {
            return DateUtils.formatDate(dateTime, DateUtils.AUDIT_DATE_FORMAT);
        }
        return null;
    }
}
