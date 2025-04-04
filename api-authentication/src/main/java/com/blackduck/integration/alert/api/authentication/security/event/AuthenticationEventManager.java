/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.api.authentication.security.event;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.api.event.EventManager;
import com.blackduck.integration.alert.common.enumeration.AuthenticationType;
import com.blackduck.integration.alert.common.persistence.model.UserModel;
import com.blackduck.integration.alert.common.persistence.model.UserRoleModel;

@Component
public class AuthenticationEventManager {
    private final Logger logger = LoggerFactory.getLogger(AuthenticationEventManager.class);
    private final EventManager eventManager;

    @Autowired
    public AuthenticationEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public void sendAuthenticationEvent(Authentication authentication, AuthenticationType authenticationType) {
        String emailAddress;
        try {
            String username = authentication.getName();
            Object authPrincipal = authentication.getPrincipal();
            if (authPrincipal instanceof InetOrgPerson) {
                emailAddress = ((InetOrgPerson) authPrincipal).getMail();
            } else {
                emailAddress = StringUtils.contains(username, "@") ? username : null;
            }
            sendAuthenticationEvent(username, emailAddress, authenticationType, authentication.getAuthorities());
        } catch (Exception e) {
            logger.warn("Unable to send authentication event");
            logger.debug("Authentication event failure", e);
        }
    }

    public Optional<String> getRoleFromAuthority(GrantedAuthority grantedAuthority) {
        String authority = grantedAuthority.getAuthority();
        if (authority.startsWith(UserModel.ROLE_PREFIX)) {
            String roleName = StringUtils.substringAfter(authority, UserModel.ROLE_PREFIX);
            return Optional.of(roleName);
        }
        return Optional.empty();
    }

    private void sendAuthenticationEvent(String username, String emailAddress, AuthenticationType authenticationType, Collection<? extends GrantedAuthority> authorities) throws AlertException {
        if (username == null) {
            throw new AlertException("Unable to send authentication event with null username");
        }
        Set<UserRoleModel> alertRoles = authorities
            .stream()
            .map(this::getRoleFromAuthority)
            .flatMap(Optional::stream)
            .map(UserRoleModel::of)
            .collect(Collectors.toSet());
        // The database users will not be enabled because they already exist in the database when this is called. So a new entry will not be added to the database.
        UserModel userModel = UserModel.loggedInUser(username, null, emailAddress, authenticationType, alertRoles, true);
        sendAuthenticationEvent(userModel);
    }

    private void sendAuthenticationEvent(UserModel userModel) {
        AlertAuthenticationEvent authEvent = new AlertAuthenticationEvent(userModel);

        // this event is used primarily to update the user database with the authentication type. LDAP or SAML are the most important.
        // if the message queue broker is full having a new thread will block that thread until the queue can produce more events.
        // By using a new thread this allows the user to be able to authenticate with Alert.
        ExecutorService userUpdateThread = Executors.newSingleThreadExecutor();
        userUpdateThread.submit(() -> eventManager.sendEvent(authEvent));
    }

}
