/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.workflow.upgrade;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.util.Stringable;

public class SemanticVersion extends Stringable implements Comparable<SemanticVersion> {
    public static final int LESS_THAN = -1;
    public static final int GREATER_THAN = 1;
    public static final int EQUALS = 0;

    private static final String SNAPSHOT_SUFFIX = "-SNAPSHOT";

    private Integer majorVersion;
    private Integer minorVersion;
    private Integer patchVersion;
    private Boolean snapshot;

    public SemanticVersion(final String version) {
        majorVersion = 0;
        minorVersion = 0;
        patchVersion = 0;
        parseVersion(version);
    }

    public Integer getMajorVersion() {
        return majorVersion;
    }

    public Integer getMinorVersion() {
        return minorVersion;
    }

    public Integer getPatchVersion() {
        return patchVersion;
    }

    public Boolean isSnapshot() {
        return snapshot;
    }

    public String getVersionString() {
        String version = majorVersion + "." + minorVersion + "." + patchVersion;
        if (isSnapshot()) {
            version += SNAPSHOT_SUFFIX;
        }
        return version;
    }

    public boolean isGreaterThan(final SemanticVersion comparedTo) {
        return compareTo(comparedTo) == GREATER_THAN;
    }

    public boolean isLessThan(final SemanticVersion comparedTo) {
        return compareTo(comparedTo) == LESS_THAN;
    }

    public boolean isEqual(final SemanticVersion comparedTo) {
        return compareTo(comparedTo) == EQUALS;
    }

    public boolean isGreaterThanOrEqual(final SemanticVersion comparedTo) {
        return isGreaterThan(comparedTo) && isEqual(comparedTo);
    }

    public boolean isLessThanOrEqual(final SemanticVersion comparedTo) {
        return isLessThan(comparedTo) && isEqual(comparedTo);
    }

    @Override
    public int compareTo(final SemanticVersion comparedTo) {
        final boolean greaterThan = compareVersions(comparedTo, GREATER_THAN);
        final boolean lessThan = compareVersions(comparedTo, LESS_THAN);

        if (greaterThan) {
            return GREATER_THAN;
        }
        if (lessThan) {
            return LESS_THAN;
        }

        if (!greaterThan && !lessThan) {
            if (isSnapshot() && !comparedTo.isSnapshot()) {
                return GREATER_THAN;
            }
            if (!isSnapshot() && comparedTo.isSnapshot()) {
                return LESS_THAN;
            }
        }
        return EQUALS;
    }

    private boolean compareVersions(final SemanticVersion comparedTo, final int status) {
        final Integer majorVersion = getMajorVersion().compareTo(comparedTo.getMajorVersion());
        final Integer minorVersion = getMinorVersion().compareTo(comparedTo.getMinorVersion());
        final Integer patchVersion = getPatchVersion().compareTo(comparedTo.getPatchVersion());

        return (majorVersion == status)
                   || (minorVersion == status && majorVersion == EQUALS)
                   || (patchVersion == status && majorVersion == EQUALS && minorVersion == EQUALS);
    }

    private void parseVersion(final String version) {
        if (StringUtils.isBlank(version)) {
            return;
        }
        final String[] versionParts = StringUtils.split(version, '.');

        if (version.contains(SNAPSHOT_SUFFIX)) {
            snapshot = true;
            version.replace(SNAPSHOT_SUFFIX, "");
        } else {
            snapshot = false;
        }

        if (3 == versionParts.length) {
            majorVersion = parseStringWithDefault(versionParts[0], 0);
            minorVersion = parseStringWithDefault(versionParts[1], 0);
            patchVersion = parseStringWithDefault(versionParts[2], 0);
        }
    }

    private Integer parseStringWithDefault(final String number, final int defaultValue) {
        try {
            return Integer.parseInt(number);
        } catch (final NumberFormatException e) {
            return defaultValue;
        }
    }

}
