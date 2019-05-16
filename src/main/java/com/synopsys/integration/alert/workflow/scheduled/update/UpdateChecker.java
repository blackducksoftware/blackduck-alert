/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.workflow.scheduled.update;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.AboutReader;
import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.workflow.scheduled.update.model.DockerTagModel;
import com.synopsys.integration.alert.workflow.scheduled.update.model.DockerTagsResponseModel;
import com.synopsys.integration.alert.workflow.scheduled.update.model.UpdateModel;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class UpdateChecker {
    private static final char VERSION_SEPARATOR = '.';

    private final Logger logger = LoggerFactory.getLogger(UpdateChecker.class);
    private final Gson gson;
    private final AboutReader aboutReader;
    private final ProxyManager proxyManager;

    @Autowired
    public UpdateChecker(final Gson gson, final AboutReader aboutReader, final ProxyManager proxyManager) {
        this.gson = gson;
        this.aboutReader = aboutReader;
        this.proxyManager = proxyManager;
    }

    public UpdateModel getUpdateModel() {
        final IntHttpClient intHttpClient = createHttpClient();
        final DockerTagRetriever dockerTagRetriever = new DockerTagRetriever(gson, intHttpClient);

        final String currentVersion = aboutReader.getProductVersion();
        final Optional<String> optionalLatestAvailableVersion = getLatestAvailableVersion(dockerTagRetriever);
        final String repositoryUrl = dockerTagRetriever.getRepositoryUrl();
        return new UpdateModel(currentVersion, optionalLatestAvailableVersion.orElse(null), repositoryUrl);
    }

    private IntHttpClient createHttpClient() {
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        final ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        return new IntHttpClient(intLogger, 120, false, proxyInfo);
    }

    private Optional<String> getLatestAvailableVersion(final DockerTagRetriever dockerTagRetriever) {
        DockerTagsResponseModel tagsResponseModel = dockerTagRetriever.getTagsModel();

        final List<String> versionCandidates = new LinkedList<>();
        while (!tagsResponseModel.isEmpty()) {
            versionCandidates.addAll(getVersionCandidates(tagsResponseModel));
            tagsResponseModel = dockerTagRetriever.getNextPage(tagsResponseModel);
        }

        return versionCandidates
                   .stream()
                   .min(versionOrder());
    }

    private List<String> getVersionCandidates(final DockerTagsResponseModel tagsResponseModel) {
        return tagsResponseModel.getResults()
                   .stream()
                   .map(DockerTagModel::getName)
                   .filter(this::isProductionVersion)
                   .collect(Collectors.toList());
    }

    private boolean isProductionVersion(final String version) {
        return StringUtils.isNotBlank(version) && !version.contains("SNAPSHOT") && isComprisedOfNumericTokens(version);
    }

    private boolean isComprisedOfNumericTokens(final String version) {
        final String[] versionTokens = StringUtils.split(version, VERSION_SEPARATOR);
        return Stream
                   .of(versionTokens)
                   .allMatch(NumberUtils::isParsable);
    }

    private Comparator<String> versionOrder() {
        return (firstVersion, secondVersion) -> {
            final String[] firstVersionTokens = StringUtils.split(firstVersion, VERSION_SEPARATOR);
            final String[] secondVersionTokens = StringUtils.split(secondVersion, VERSION_SEPARATOR);

            for (int i = 0; i < firstVersionTokens.length && i < secondVersionTokens.length; i++) {
                final int firstToken = Integer.parseInt(firstVersionTokens[i]);
                final int secondToken = Integer.parseInt(secondVersionTokens[i]);

                // If the first token is greater, it is a newer version.
                if (firstToken > secondToken) {
                    return -1;
                } else if (secondToken > firstToken) {
                    return 1;
                }
            }

            // If their versions have been the same up to this point, a patch release would have more tokens, making it the newer version.
            if (firstVersionTokens.length > secondVersionTokens.length) {
                return -1;
            } else if (secondVersionTokens.length > firstVersionTokens.length) {
                return 1;
            }
            return 0;
        };
    }

}
