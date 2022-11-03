package com.synopsys.integration.alert.channel.github.descriptor;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class GitHubDescriptor extends ChannelDescriptor {
    public static final String GITHUB_PREFIX = "github.";
    public static final String GITHUB_REPOSITORY_NAME = GITHUB_PREFIX + "repository.name";
    public static final String GITHUB_PR_TITLE_PREFIX = GITHUB_PREFIX + "pr.title.prefix";

    @Autowired
    public GitHubDescriptor() {
        super(ChannelKeys.GITHUB, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION));
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.empty();
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.empty();
    }
}
