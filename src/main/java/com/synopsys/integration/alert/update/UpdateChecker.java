/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.update;

import static com.synopsys.integration.alert.common.util.DateUtils.DOCKER_DATE_FORMAT;
import static com.synopsys.integration.alert.common.util.DateUtils.parseDate;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.rest.proxy.ProxyManager;
import com.synopsys.integration.alert.update.model.DockerTagModel;
import com.synopsys.integration.alert.update.model.DockerTagsResponseModel;
import com.synopsys.integration.alert.update.model.UpdateModel;
import com.synopsys.integration.alert.web.api.about.AboutModel;
import com.synopsys.integration.alert.web.api.about.AboutReader;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;

@Component
public class UpdateChecker {
    private static final String SNAPSHOT = "-SNAPSHOT";
    private static final String QA_BUILD = "-SIGQA";
    private static final char VERSION_SEPARATOR = '.';

    private final IntLogger logger = new Slf4jIntLogger(LoggerFactory.getLogger(getClass()));
    private final Gson gson;
    private final AboutReader aboutReader;
    private final ProxyManager proxyManager;
    private final AlertProperties alertProperties;

    @Autowired
    public UpdateChecker(Gson gson, AboutReader aboutReader, ProxyManager proxyManager, AlertProperties alertProperties) {
        this.gson = gson;
        this.aboutReader = aboutReader;
        this.proxyManager = proxyManager;
        this.alertProperties = alertProperties;
    }

    public UpdateModel getUpdateModel() {
        IntHttpClient intHttpClient = createHttpClient();
        DockerTagRetriever dockerTagRetriever = new DockerTagRetriever(gson, intHttpClient);

        Optional<AboutModel> aboutModel = aboutReader.getAboutModel();
        String currentVersion = aboutModel.map(AboutModel::getVersion).orElse(null);
        String alertCreated = aboutModel.map(AboutModel::getCreated).orElse(null);

        boolean isProduction = isProductionVersion(currentVersion);

        Optional<VersionDateModel> latestAvailableVersion = getLatestAvailableTag(dockerTagRetriever, isProduction);
        String repositoryUrl = dockerTagRetriever.getRepositoryUrl();

        // latestAvailableVersion will not be present if Alert can not reach Docker Hub and DockerTagRetriever will log a warning
        // if latestAvailableVersion is empty, use the Alert version and date so we report no update available
        String latestVersion = latestAvailableVersion.map(VersionDateModel::getVersionName).orElse(currentVersion);
        String latestDate = latestAvailableVersion.map(VersionDateModel::getDate).orElse(alertCreated);
        return getUpdateModel(currentVersion, alertCreated, latestVersion, latestDate, repositoryUrl);
    }

    public UpdateModel getUpdateModel(String currentVersion, String alertCreated, String dockerTagVersionName, String dockerTagUpdatedDate, String repositoryUrl) {
        VersionDateModel alertModel = new VersionDateModel(currentVersion, alertCreated);
        VersionDateModel dockerTagModel = new VersionDateModel(dockerTagVersionName, dockerTagUpdatedDate);

        boolean isProduction = isProductionVersion(alertModel.getVersionName());

        int comparison;
        if (isProduction) {
            comparison = compareVersions(alertModel.getVersionName(), dockerTagModel.getVersionName());
        } else {
            comparison = versionDateModelComparator().compare(alertModel, dockerTagModel);
        }

        boolean isUpdatable = 1 == comparison;
        return new UpdateModel(currentVersion, alertCreated, dockerTagVersionName, dockerTagUpdatedDate, repositoryUrl, isUpdatable);
    }

    private IntHttpClient createHttpClient() {
        ProxyInfo proxyInfo = proxyManager.createProxyInfoForHost(DockerTagRetriever.ALERT_DOCKER_REGISTRY_URL);
        Boolean alwaysTrustServerCert = alertProperties.getAlertTrustCertificate().orElse(Boolean.FALSE);
        return new IntHttpClient(logger, gson, 120, alwaysTrustServerCert, proxyInfo);
    }

    private Optional<VersionDateModel> getLatestAvailableTag(DockerTagRetriever dockerTagRetriever, boolean isProduction) {
        DockerTagsResponseModel tagsResponseModel = dockerTagRetriever.getTagsModel();

        List<DockerTagModel> tags = new LinkedList<>();
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
                   .map(dockerTagModel -> new VersionDateModel(dockerTagModel.getName(), dockerTagModel.getLastUpdated()))
                   .min(versionDateModelComparator());
    }

    private boolean isProductionVersion(String version) {
        return StringUtils.isNotBlank(version) && !version.contains(SNAPSHOT) && !version.contains(QA_BUILD) && isComprisedOfNumericTokens(version);
    }

    private boolean isComprisedOfNumericTokens(String version) {
        String[] versionTokens = StringUtils.split(version, VERSION_SEPARATOR);
        return Stream
                   .of(versionTokens)
                   .allMatch(NumberUtils::isParsable);
    }

    private Comparator<VersionDateModel> versionDateModelComparator() {
        return (firstModel, secondModel) -> {
            int comparison = compareVersions(firstModel.getVersionName(), secondModel.getVersionName());
            if (0 == comparison) {
                comparison = compareDateStrings(firstModel.getDate(), secondModel.getDate());
            }
            if (0 == comparison) {
                comparison = compareProductionAndSnapshotVersions(firstModel.getVersionName(), secondModel.getVersionName());
            }
            return comparison;
        };
    }

    private int compareVersions(String firstVersion, String secondVersion) {
        String[] firstVersionTokens = StringUtils.split(firstVersion, VERSION_SEPARATOR);
        String[] secondVersionTokens = StringUtils.split(secondVersion, VERSION_SEPARATOR);

        for (int i = 0; i < firstVersionTokens.length && i < secondVersionTokens.length; i++) {
            String firstVersionToken = firstVersionTokens[i];
            String secondVersionToken = secondVersionTokens[i];

            firstVersionToken = getVersionToken(firstVersionToken);
            secondVersionToken = getVersionToken(secondVersionToken);

            int firstToken = Integer.parseInt(firstVersionToken);
            int secondToken = Integer.parseInt(secondVersionToken);

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
    }

    private String getVersionToken(String versionToken) {
        String resultToken = versionToken;
        int indexOfHyphen = versionToken.indexOf("-");
        if (indexOfHyphen > 0) {
            resultToken = versionToken.substring(0, indexOfHyphen);
        }

        return resultToken;
    }

    private int compareProductionAndSnapshotVersions(String firstVersion, String secondVersion) {
        boolean firstIsProduction = isProductionVersion(firstVersion);
        boolean secondIsProduction = isProductionVersion(secondVersion);

        if (firstIsProduction && !secondIsProduction) {
            return -1;
        } else if (!firstIsProduction && secondIsProduction) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * The Date Alert has in the About will always be slightly before the Docker Hub date. So if the two are within 1 hour of each other, then we will consider them the same.
     */
    private int compareDateStrings(String first, String second) {
        try {
            OffsetDateTime firstDate = parseDate(first, DOCKER_DATE_FORMAT);
            OffsetDateTime secondDate = parseDate(second, DOCKER_DATE_FORMAT);

            OffsetDateTime hourEarlier = firstDate.minusHours(1);
            OffsetDateTime hourLater = firstDate.plusHours(1);

            boolean secondIsWithinAnHourOfFirst = hourEarlier.isBefore(secondDate) && hourLater.isAfter(secondDate);

            if (secondIsWithinAnHourOfFirst) {
                return 0;
            }
            if (firstDate.isAfter(secondDate)) {
                return -1;
            } else if (firstDate.isBefore(secondDate)) {
                return 1;
            }
        } catch (ParseException e) {
            logger.debug(String.format("Could not parse the date strings with the format %s.", DOCKER_DATE_FORMAT), e);
        }
        return 0;
    }

    private class VersionDateModel {
        private final String versionName;
        private final String date;

        private VersionDateModel(String versionName, String date) {
            this.versionName = versionName;
            this.date = date;
        }

        public String getVersionName() {
            return versionName;
        }

        public String getDate() {
            return date;
        }

    }

}
