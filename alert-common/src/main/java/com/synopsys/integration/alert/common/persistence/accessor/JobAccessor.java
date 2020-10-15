package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;

public interface JobAccessor {
    List<ConfigurationJobModel> getAllJobs();

    Optional<ConfigurationJobModel> getJobById(UUID jobId) throws AlertDatabaseConstraintException;

    List<ConfigurationJobModel> getJobsByFrequency(FrequencyType frequency);

    ConfigurationJobModel createJob(Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    ConfigurationJobModel updateJob(UUID jobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException;

    void deleteJob(UUID jobId) throws AlertDatabaseConstraintException;

}
