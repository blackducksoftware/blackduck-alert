/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.model.UserModel;

public interface UserAccessor {
    Long DEFAULT_ADMIN_USER_ID = 1L;
    Long DEFAULT_JOB_MANAGER_ID = 2L;
    Long DEFAULT_ALERT_USER_ID = 3L;

    List<UserModel> getUsers();

    Optional<UserModel> getUser(Long userId);

    Optional<UserModel> getUser(String username);

    UserModel addUser(String userName, String password, String emailAddress) throws AlertConfigurationException;

    UserModel addUser(UserModel user, boolean passwordEncoded) throws AlertConfigurationException;

    UserModel updateUser(UserModel user, boolean passwordEncoded) throws AlertConfigurationException, AlertForbiddenOperationException;

    boolean assignRoles(String username, Set<Long> roleIds);

    boolean changeUserPassword(String username, String newPassword);

    boolean changeUserEmailAddress(String username, String emailAddress);

    void deleteUser(String userName) throws AlertForbiddenOperationException;

    void deleteUser(Long userId) throws AlertForbiddenOperationException;

}
