package com.synopsys.integration.alert.processor.api;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;

import com.synopsys.integration.alert.common.persistence.accessor.ProcessingJobAccessor;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.FilteredDistributionJobResponseModel;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;

public class MockProcessingJobAccessor implements ProcessingJobAccessor {
    private final List<FilteredDistributionJobResponseModel> storedJobs;

    public MockProcessingJobAccessor(List<FilteredDistributionJobResponseModel> nonMatchingJobs, FilteredDistributionJobResponseModel matchingJob, int matchingJobPosition) {
        this.storedJobs = initStoredJobs(nonMatchingJobs, matchingJob, matchingJobPosition);
    }

    @Override
    public AlertPagedModel<FilteredDistributionJobResponseModel> getMatchingEnabledJobsByFilteredNotifications(FilteredDistributionJobRequestModel filter, int pageOffset, int pageLimit) {
        // Mocking JPA means pageOffset starts at zero and increments by one for each disjoint page.
        List<List<FilteredDistributionJobResponseModel>> storedJobPages = ListUtils.partition(storedJobs, pageLimit);
        List<FilteredDistributionJobResponseModel> pageFromOffset = extractPageSafely(storedJobPages, pageOffset);
        List<FilteredDistributionJobResponseModel> filteredPage = filterPage(filter, pageFromOffset);

        return new AlertPagedModel<>(storedJobPages.size(), pageOffset, pageLimit, filteredPage);
    }

    private List<FilteredDistributionJobResponseModel> extractPageSafely(List<List<FilteredDistributionJobResponseModel>> storedJobPages, int pageNumber) {
        try {
            return storedJobPages.get(pageNumber);
        } catch (IndexOutOfBoundsException e) {
            return List.of();
        }
    }

    private List<FilteredDistributionJobResponseModel> filterPage(FilteredDistributionJobRequestModel filter, List<FilteredDistributionJobResponseModel> pageOfJobs) {
        return pageOfJobs
            .stream()
            .filter(job -> filterJob(filter, job))
            .collect(Collectors.toList());
    }

    private boolean filterJob(FilteredDistributionJobRequestModel filter, FilteredDistributionJobResponseModel job) {
        // Mocking the database filtering is currently unnecessary because we are trying to test in-memory filtering
        return true;
    }

    private static List<FilteredDistributionJobResponseModel> initStoredJobs(List<FilteredDistributionJobResponseModel> nonMatchingJobs, FilteredDistributionJobResponseModel matchingJob, int matchingJobPosition) {
        List<FilteredDistributionJobResponseModel> updatedJobList = new ArrayList<>(nonMatchingJobs.size() + 1);
        updatedJobList.addAll(nonMatchingJobs);

        int lastIndex = updatedJobList.size() - 1;
        if (matchingJobPosition < 1) {
            updatedJobList.add(0, matchingJob);
        } else {
            int smallerIndex = Math.min(matchingJobPosition, lastIndex);
            updatedJobList.add(smallerIndex, matchingJob);
        }

        return updatedJobList;
    }

}
