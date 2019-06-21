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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.AboutReader;
import com.synopsys.integration.alert.ProxyManager;
import com.synopsys.integration.alert.web.model.AboutModel;
import com.synopsys.integration.alert.workflow.scheduled.update.model.DockerTagModel;
import com.synopsys.integration.alert.workflow.scheduled.update.model.DockerTagsResponseModel;
import com.synopsys.integration.alert.workflow.scheduled.update.model.UpdateModel;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class UpdateChecker {
    public static final String DATE_FORMAT = "yyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'";
    private static final String SNAPSHOT = "-SNAPSHOT";
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

        final AboutModel aboutModel = aboutReader.getAboutModel();
        final String currentVersion = aboutModel.getVersion();
        final String alertCreated = aboutModel.getCreated();

        final Boolean isProduction = isProductionVersion(currentVersion);

        final DockerTagModel latestAvailableVersion = getLatestAvailableTag(dockerTagRetriever, isProduction);
        final String repositoryUrl = dockerTagRetriever.getRepositoryUrl();

        return getUpdateModel(currentVersion, alertCreated, latestAvailableVersion, repositoryUrl);
    }

    public UpdateModel getUpdateModel(final String currentVersion, final String alertCreated, final DockerTagModel latestAvailableVersion, final String repositoryUrl) {
        final boolean isUpdatable = 1 == compareVersionAndDates(currentVersion, alertCreated, latestAvailableVersion.getName(), latestAvailableVersion.getLastUpdated(), this::compareDateStrings);
        return new UpdateModel(currentVersion, alertCreated, latestAvailableVersion, repositoryUrl, isUpdatable);
    }

    private IntHttpClient createHttpClient() {
        final IntLogger intLogger = new Slf4jIntLogger(logger);
        final ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        return new IntHttpClient(intLogger, 120, false, proxyInfo);
    }

    private DockerTagModel getLatestAvailableTag(final DockerTagRetriever dockerTagRetriever, final boolean isProduction) {
        DockerTagsResponseModel tagsResponseModel = dockerTagRetriever.getTagsModel();

        final List<DockerTagModel> tags = new LinkedList<>();
        while (!tagsResponseModel.isEmpty()) {
            tags.addAll(tagsResponseModel.getResults());
            tagsResponseModel = dockerTagRetriever.getNextPage(tagsResponseModel);
        }

        return tags
                   .stream()
                   .filter(dockerTagModel -> {
                       if (isProduction) {
                           return isProductionVersion(dockerTagModel.getName());
                       }
                       return true;
                   })
                   .min(tagOrder())
                   .get();
    }

    private boolean isProductionVersion(final String version) {
        return StringUtils.isNotBlank(version) && !version.contains(SNAPSHOT) && isComprisedOfNumericTokens(version);
    }

    private boolean isComprisedOfNumericTokens(final String version) {
        final String[] versionTokens = StringUtils.split(version, VERSION_SEPARATOR);
        return Stream
                   .of(versionTokens)
                   .allMatch(NumberUtils::isParsable);
    }

    private Comparator<DockerTagModel> tagOrder() {
        return (firstTag, secondTag) -> compareVersionAndDates(firstTag.getName(), firstTag.getLastUpdated(), secondTag.getName(), secondTag.getLastUpdated(), this::compareDateStrings);
    }

    private int compareVersionAndDates(final String firstVersion, final String firstDate, final String secondVersion, final String secondDate, final BiFunction<String, String, Integer> dateComparison) {
        final String[] firstVersionTokens = StringUtils.split(firstVersion, VERSION_SEPARATOR);
        final String[] secondVersionTokens = StringUtils.split(secondVersion, VERSION_SEPARATOR);

        for (int i = 0; i < firstVersionTokens.length && i < secondVersionTokens.length; i++) {
            String firstVersionToken = firstVersionTokens[i];
            boolean firstSnapshot = false;
            String secondVersionToken = secondVersionTokens[i];
            boolean secondSnapshot = false;
            if (firstVersionToken.contains(SNAPSHOT)) {
                firstVersionToken = firstVersionToken.substring(0, firstVersionToken.indexOf(SNAPSHOT));
                firstSnapshot = true;
            }
            if (secondVersionToken.contains(SNAPSHOT)) {
                secondVersionToken = secondVersionToken.substring(0, secondVersionToken.indexOf(SNAPSHOT));
                secondSnapshot = true;
            }

            final int firstToken = Integer.parseInt(firstVersionToken);
            final int secondToken = Integer.parseInt(secondVersionToken);

            // If the first token is greater, it is a newer version.
            if (firstToken > secondToken) {
                return -1;
            } else if (secondToken > firstToken) {
                return 1;
            } else if (firstSnapshot || secondSnapshot) {
                final Integer comparison = dateComparison.apply(firstDate, secondDate);
                if (0 != comparison) {
                    return comparison;
                } else if (firstSnapshot && !secondSnapshot) {
                    return 1;
                } else if (!firstSnapshot && secondSnapshot) {
                    return -1;
                }
            }
        }
        // If their versions have been the same up to this point, a patch release would have more tokens, making it the newer version.
        if (firstVersionTokens.length > secondVersionTokens.length) {
            return -1;
        } else if (secondVersionTokens.length > firstVersionTokens.length) {
            return 1;
        }
        return 0;
    }

    /**
     * The Date Alert has in the About will always be slightly before the Docker Hub date. So if the two are within 1 hour of each other, then we will consider them the same.
     */
    private int compareDateStrings(final String first, final String second) {
        try {
            final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            final Date firstDate = formatter.parse(first);
            final Date secondDate = formatter.parse(second);

            final Date hourEarlier = DateUtils.addHours(firstDate, -1);
            final Date hourLater = DateUtils.addHours(firstDate, 1);

            final boolean secondIsWithinAnHourOfFirst = hourEarlier.before(secondDate) && hourLater.after(secondDate);

            if (secondIsWithinAnHourOfFirst) {
                return 0;
            }
            if (firstDate.after(secondDate)) {
                return -1;
            } else if (firstDate.before(secondDate)) {
                return 1;
            }
        } catch (final ParseException e) {
            logger.debug("Could not parse the date strings with the format {}.", DATE_FORMAT);
            logger.debug(e.getMessage(), e);
        }
        return 0;
    }

}
