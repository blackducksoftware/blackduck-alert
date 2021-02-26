/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.channel.issuetracker.service;

import com.synopsys.integration.exception.IntegrationException;

/**
 * @param <T> A class that represents a transition.
 */
public interface TransitionValidator<T> {
    boolean doesTransitionToExpectedStatusCategory(T transition, String expectedStatusCategoryKey) throws IntegrationException;

}
