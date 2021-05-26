/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.distribution.util;

import java.util.Optional;

import com.google.api.client.http.GenericUrl;
import com.synopsys.integration.azure.boards.common.http.AzureHttpServiceFactory;
import com.synopsys.integration.azure.boards.common.model.ReferenceLinkModel;
import com.synopsys.integration.azure.boards.common.service.workitem.response.WorkItemResponseModel;

public final class AzureBoardsUILinkUtils {
    public static String extractUILink(String organizationName, WorkItemResponseModel workItem) {
        return Optional.ofNullable(workItem.getLinks())
                   .flatMap(issueLinkMap -> Optional.ofNullable(issueLinkMap.get("html")))
                   .map(ReferenceLinkModel::getHref)
                   .orElseGet(() -> createIssueTrackerUrl(organizationName));
    }

    private static String createIssueTrackerUrl(String organizationName) {
        String url = String.format("%s/%s", AzureHttpServiceFactory.DEFAULT_BASE_URL, organizationName);
        return new GenericUrl(url).build();
    }

    private AzureBoardsUILinkUtils() {
    }

}
