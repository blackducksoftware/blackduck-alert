package com.blackduck.integration.alert.database.job.msteams;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.blackduck.integration.alert.common.persistence.accessor.MSTeamsJobDetailsAccessor;
import com.blackduck.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;

@Component
public class DefaultMSTeamsJobDetailsAccessor implements MSTeamsJobDetailsAccessor {
    private final MSTeamsJobDetailsRepository msTeamsJobDetailsRepository;

    @Autowired
    public DefaultMSTeamsJobDetailsAccessor(MSTeamsJobDetailsRepository msTeamsJobDetailsRepository) {
        this.msTeamsJobDetailsRepository = msTeamsJobDetailsRepository;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public MSTeamsJobDetailsEntity saveMSTeamsJobDetails(UUID jobId, MSTeamsJobDetailsModel msTeamsJobDetails) {
        MSTeamsJobDetailsEntity jobDetailsToSave = new MSTeamsJobDetailsEntity(jobId, msTeamsJobDetails.getWebhook());
        return msTeamsJobDetailsRepository.save(jobDetailsToSave);
    }

    @Override
    public Optional<MSTeamsJobDetailsModel> retrieveDetails(UUID jobId) {
        return msTeamsJobDetailsRepository.findById(jobId)
                   .map(entity -> new MSTeamsJobDetailsModel(entity.getJobId(), entity.getWebhook()));
    }
}
