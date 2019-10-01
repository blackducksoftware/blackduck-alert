package com.synopsys.integration.alert.provider.blackduck.new_collector.util;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.generated.view.VersionBomComponentView;

@Component
public class NotificationDataExtractor {

    public Optional<String> getProjectComponentQueryLink(BlackDuckResponseCache responseCache, String projectVersionUrl, String link, String componentName) {
        Optional<String> projectLink = getProjectLink(responseCache, projectVersionUrl, link);
        return projectLink.flatMap(optionalProjectLink -> getProjectComponentQueryLink(optionalProjectLink, componentName));
    }

    public Optional<String> getProjectComponentQueryLink(String projectLink, String componentName) {
        return Optional.of(String.format("%s?q=componentName:%s", projectLink, componentName));
    }

    public Optional<String> getProjectLink(BlackDuckResponseCache blackDuckResponseCache, String projectVersionUrl, String link) {
        Optional<ProjectVersionView> optionalProjectVersionFuture = blackDuckResponseCache.getItem(ProjectVersionView.class, projectVersionUrl);
        return optionalProjectVersionFuture
                   .flatMap(view -> view.getFirstLink(link));
    }

    public Optional<VersionBomComponentView> getBomComponentView(BlackDuckResponseCache blackDuckResponseCache, String bomComponentUrl) {
        if (org.apache.commons.lang.StringUtils.isNotBlank(bomComponentUrl)) {
            return blackDuckResponseCache.getItem(VersionBomComponentView.class, bomComponentUrl);
        }
        return Optional.empty();
    }
}
