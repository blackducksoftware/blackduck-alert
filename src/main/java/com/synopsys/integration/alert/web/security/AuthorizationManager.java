package com.synopsys.integration.alert.web.security;

import org.springframework.security.core.userdetails.UserDetails;

import com.synopsys.integration.alert.common.enumeration.AccessOperation;

public class AuthorizationManager {

    public boolean hasPermission(final UserDetails userDetails, final String permissionKey, final AccessOperation operation) {
        return true;
    }
}
