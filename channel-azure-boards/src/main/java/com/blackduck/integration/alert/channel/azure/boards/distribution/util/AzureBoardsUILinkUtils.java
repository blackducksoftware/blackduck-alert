package com.blackduck.integration.alert.channel.azure.boards.distribution.util;

import java.util.Optional;

import com.blackduck.integration.alert.azure.boards.common.http.AzureHttpRequestCreatorFactory;
import com.blackduck.integration.alert.azure.boards.common.model.ReferenceLinkModel;
import com.blackduck.integration.alert.azure.boards.common.service.workitem.response.WorkItemResponseModel;
import com.google.api.client.http.GenericUrl;

public final class AzureBoardsUILinkUtils {
    public static String extractUILink(String organizationName, WorkItemResponseModel workItem) {
        return Optional.ofNullable(workItem.getLinks())
            .flatMap(issueLinkMap -> Optional.ofNullable(issueLinkMap.get("html")))
            .map(ReferenceLinkModel::getHref)
            .orElseGet(() -> createIssueTrackerUrl(organizationName));
    }

    private static String createIssueTrackerUrl(String organizationName) {
        String url = String.format("%s/%s", AzureHttpRequestCreatorFactory.DEFAULT_BASE_URL, organizationName);
        return new GenericUrl(url).build();
    }

    private AzureBoardsUILinkUtils() {
    }

}
