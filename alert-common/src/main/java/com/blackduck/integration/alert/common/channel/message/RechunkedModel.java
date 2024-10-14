/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.channel.message;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class RechunkedModel extends AlertSerializableModel {
    private final String firstChunk;
    private final List<String> remainingChunks;

    public RechunkedModel(String firstChunk, List<String> remainingChunks) {
        this.firstChunk = firstChunk;
        this.remainingChunks = remainingChunks;
    }

    public String getFirstChunk() {
        return firstChunk;
    }

    public List<String> getRemainingChunks() {
        return remainingChunks;
    }

}
