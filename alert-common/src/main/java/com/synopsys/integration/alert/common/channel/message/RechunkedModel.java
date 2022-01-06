/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.message;

import java.util.List;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

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
