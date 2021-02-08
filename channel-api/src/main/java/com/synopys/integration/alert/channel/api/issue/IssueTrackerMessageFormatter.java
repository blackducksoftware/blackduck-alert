/*
 * channel-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopys.integration.alert.channel.api.issue;

import com.synopys.integration.alert.channel.api.convert.ChannelMessageFormatter;

public abstract class IssueTrackerMessageFormatter extends ChannelMessageFormatter {
    private final int maxTitleLength;
    private final int maxCommentLength;

    public IssueTrackerMessageFormatter(int maxTitleLength, int maxDescriptionLength, int maxCommentLength, String lineSeparator) {
        super(maxDescriptionLength, lineSeparator);
        this.maxTitleLength = maxTitleLength;
        this.maxCommentLength = maxCommentLength;
    }

    public IssueTrackerMessageFormatter(int maxTitleLength, int maxDescriptionLength, int maxCommentLength, String lineSeparator, String sectionSeparator, String nonBreakingSpace) {
        super(maxDescriptionLength, lineSeparator, sectionSeparator, nonBreakingSpace);
        this.maxTitleLength = maxTitleLength;
        this.maxCommentLength = maxCommentLength;
    }

    public int getMaxTitleLength() {
        return maxTitleLength;
    }

    public int getMaxCommentLength() {
        return maxCommentLength;
    }

    /**
     * Alias for getMaxMessageLength()
     * @return getMaxMessageLength()
     */
    public int getMaxDescriptionLength() {
        return getMaxMessageLength();
    }

}
