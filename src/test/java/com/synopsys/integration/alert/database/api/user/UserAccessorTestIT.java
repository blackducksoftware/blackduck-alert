package com.synopsys.integration.alert.database.api.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;

public class UserAccessorTestIT extends AlertIntegrationTest {

    @Autowired
    private UserAccessor userAccessor;

    @Test
    public void testGetUserByUserName() {
        final Optional<UserModel> user = userAccessor.getUser("sysadmin");
        assertTrue(user.isPresent());
    }

    @Test
    public void testGetUserByUserNameNotFound() {
        final Optional<UserModel> user = userAccessor.getUser("anUnknownUser");
        assertFalse(user.isPresent());
    }
}
