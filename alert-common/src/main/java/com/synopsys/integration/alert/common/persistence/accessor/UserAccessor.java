package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.synopsys.integration.alert.common.persistence.model.UserModel;

public interface UserAccessor {

    List<UserModel> getUsers();

    Optional<UserModel> getUser(final String username);

    UserModel addOrUpdateUser(final UserModel user);

    UserModel addOrUpdateUser(final UserModel user, final boolean passwordEncoded);

    UserModel addUser(final String userName, final String password, final String emailAddress);

    boolean assignRoles(final String username, final Set<Long> roleIds);

    boolean changeUserPassword(final String username, final String newPassword);

    boolean changeUserEmailAddress(final String username, final String emailAddress);

    void deleteUser(final String userName);

}
