package com.synopsys.integration.alert.common.channel.issuetracker.service;

import java.util.List;

import com.synopsys.integration.exception.IntegrationException;

public interface TransitionHandler<T> extends TransitionValidator<T> {
    List<T> retrieveIssueTransitions(String issueKey) throws IntegrationException;

    String extractTransitionName(T transition);

}
