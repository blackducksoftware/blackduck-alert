/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.service;

import java.util.List;

import com.synopsys.integration.exception.IntegrationException;

public interface TransitionHandler<T> extends TransitionValidator<T> {
    String extractTransitionName(T transition);

    List<T> retrieveIssueTransitions(String issueKey) throws IntegrationException;

}
