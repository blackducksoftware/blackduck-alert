package com.synopsys.integration.alert.provider.blackduck.tasks;

import org.junit.Test;

import com.synopsys.integration.blackduck.api.generated.view.UserView;

public class ProjectSyncTaskTest {

    public UserView createUserView(final String email) {
        final UserView userView = new UserView();
        userView.email = email;
        return userView;
    }

    @Test
    public void testRunInitial() throws Exception {

    }

    @Test
    public void testRunAdd() throws Exception {

    }

    @Test
    public void testRunDelete() throws Exception {

    }
}
