package com.synopsys.integration.alert.channel.slack.actions;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.JobDetailsProcessor;

@Component
public class SlackJobDetailsProcessor extends JobDetailsProcessor {

    @Override
    protected DistributionJobDetailsModel convertToChannelJobDetails(Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return new SlackJobDetailsModel(
            extractFieldValueOrEmptyString("channel.slack.webhook", configuredFieldsMap),
            extractFieldValueOrEmptyString("channel.slack.channel.name", configuredFieldsMap),
            extractFieldValueOrEmptyString("channel.slack.channel.username", configuredFieldsMap)
        );
    }
}
