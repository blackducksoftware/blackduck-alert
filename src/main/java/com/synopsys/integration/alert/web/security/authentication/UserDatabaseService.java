package com.synopsys.integration.alert.web.security.authentication;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.api.user.UserModel;

@Service
public class UserDatabaseService implements UserDetailsService {
    private final UserAccessor userAccessor;

    @Autowired
    public UserDatabaseService(final UserAccessor userAccessor) {
        this.userAccessor = userAccessor;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        return createUserDetails(username, userAccessor.getUser(username));
    }

    private UserDetails createUserDetails(final String userName, final Optional<UserModel> userModel) throws UsernameNotFoundException {
        final UserModel model = userModel.orElseThrow(() -> new UsernameNotFoundException(String.format("User %s not found", userName)));
        return new UserPrincipal(model);
    }
}
