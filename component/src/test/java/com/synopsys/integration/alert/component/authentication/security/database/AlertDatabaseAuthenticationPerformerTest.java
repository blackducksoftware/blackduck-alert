package com.synopsys.integration.alert.component.authentication.security.database;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;

import com.synopsys.integration.alert.api.authentication.security.event.AuthenticationEventManager;
import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.exception.AlertForbiddenOperationException;
import com.synopsys.integration.alert.common.persistence.accessor.UserAccessor;
import com.synopsys.integration.alert.common.persistence.model.UserModel;

class AlertDatabaseAuthenticationPerformerTest {
    public static final String VALID_USERNAME = "username";
    public static final String VALID_PASSWORD = "validPassword";
    public static final String INVALID_USERNAME = "invalidUsername";
    public static final String INVALID_PASSWORD = "invalidPassword";
    private AuthenticationEventManager authenticationEventManager;
    private RoleAccessor roleAccessor;
    private DaoAuthenticationProvider alertDatabaseAuthProvider;
    private UserAccessor userAccessor;
    private UserModel expectedUserAccessorResponse;

    @BeforeEach
    public void init() {
        authenticationEventManager = Mockito.mock(AuthenticationEventManager.class);
        roleAccessor = Mockito.mock(RoleAccessor.class);
        alertDatabaseAuthProvider = Mockito.mock(DaoAuthenticationProvider.class);
        userAccessor = Mockito.mock(UserAccessor.class);
    }

    @Test
    void testValidLogin() throws AlertForbiddenOperationException, AlertConfigurationException {
        AlertDatabaseAuthenticationPerformer authenticationPerformer = new AlertDatabaseAuthenticationPerformer(
            authenticationEventManager,
            roleAccessor,
            alertDatabaseAuthProvider,
            userAccessor
        );
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(VALID_USERNAME, VALID_PASSWORD);
        expectedUserAccessorResponse = createUserModel(VALID_USERNAME, VALID_PASSWORD, false, null, 0);
        mockUserAccessorMethods(false);
        mockAuthenticationResponse(authenticationToken, true);
        Optional<Authentication> authentication = authenticationPerformer.performAuthentication(authenticationToken);
        Assertions.assertTrue(authentication.map(Authentication::isAuthenticated).orElseThrow(() -> new AssertionError("User authentication expected but not found")));
    }

    @Test
    void testInvalidUsernameLogin() throws AlertForbiddenOperationException, AlertConfigurationException {
        AlertDatabaseAuthenticationPerformer authenticationPerformer = new AlertDatabaseAuthenticationPerformer(
            authenticationEventManager,
            roleAccessor,
            alertDatabaseAuthProvider,
            userAccessor
        );
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(INVALID_USERNAME, VALID_PASSWORD);
        mockUserAccessorMethods(false);
        mockAuthenticationResponse(authenticationToken, false);
        Optional<Authentication> authentication = authenticationPerformer.performAuthentication(authenticationToken);
        Assertions.assertTrue(authentication.isEmpty());
    }

    @Test
    void testInvalidPasswordLogin() throws AlertForbiddenOperationException, AlertConfigurationException {
        AlertDatabaseAuthenticationPerformer authenticationPerformer = new AlertDatabaseAuthenticationPerformer(
            authenticationEventManager,
            roleAccessor,
            alertDatabaseAuthProvider,
            userAccessor
        );
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(VALID_USERNAME, INVALID_PASSWORD);
        expectedUserAccessorResponse = createUserModel(VALID_USERNAME, VALID_PASSWORD, false, null, 0);
        mockUserAccessorMethods(false);
        mockAuthenticationResponse(authenticationToken, false);
        Optional<Authentication> authentication = authenticationPerformer.performAuthentication(authenticationToken);
        Assertions.assertTrue(authentication.isEmpty());
    }

    @Test
    void testBadCredentialsException() throws AlertForbiddenOperationException, AlertConfigurationException {
        AlertDatabaseAuthenticationPerformer authenticationPerformer = new AlertDatabaseAuthenticationPerformer(
            authenticationEventManager,
            roleAccessor,
            alertDatabaseAuthProvider,
            userAccessor
        );
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(VALID_USERNAME, INVALID_USERNAME);
        expectedUserAccessorResponse = createUserModel(VALID_USERNAME, INVALID_PASSWORD, false, null, 0);
        mockUserAccessorMethods(false);
        Mockito.when(alertDatabaseAuthProvider.authenticate(authenticationToken)).thenThrow(new BadCredentialsException("Bad user credentials exception test."));
        Optional<Authentication> authentication = authenticationPerformer.performAuthentication(authenticationToken);
        Assertions.assertTrue(authentication.isEmpty());
    }

    @Test
    void testAccountLocked() throws AlertForbiddenOperationException, AlertConfigurationException {
        int numberOfAttempts = 5;
        AlertDatabaseAuthenticationPerformer authenticationPerformer = new AlertDatabaseAuthenticationPerformer(
            authenticationEventManager,
            roleAccessor,
            alertDatabaseAuthProvider,
            userAccessor,
            numberOfAttempts,
            AlertDatabaseAuthenticationPerformer.DEFAULT_LOCKOUT_DURATION_IN_SECONDS
        );
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(VALID_USERNAME, INVALID_PASSWORD);
        expectedUserAccessorResponse = createUserModel(VALID_USERNAME, VALID_PASSWORD, false, null, 0);
        mockUserAccessorMethods(false);
        mockAuthenticationResponse(authenticationToken, false);
        long currentAttempts = 0;
        while (currentAttempts < numberOfAttempts) {
            Optional<Authentication> authenticationAttempt = authenticationPerformer.performAuthentication(authenticationToken);
            Assertions.assertTrue(authenticationAttempt.isEmpty());
            currentAttempts++;
        }

        Assertions.assertTrue(expectedUserAccessorResponse.isLocked());
        Assertions.assertEquals(numberOfAttempts, expectedUserAccessorResponse.getFailedLoginAttempts());
    }

    @Test
    void testLockoutDurationInEffect() throws AlertForbiddenOperationException, AlertConfigurationException, InterruptedException {
        int numberOfAttempts = 5;
        AlertDatabaseAuthenticationPerformer authenticationPerformer = new AlertDatabaseAuthenticationPerformer(
            authenticationEventManager,
            roleAccessor,
            alertDatabaseAuthProvider,
            userAccessor,
            numberOfAttempts,
            AlertDatabaseAuthenticationPerformer.DEFAULT_LOCKOUT_DURATION_IN_SECONDS
        );
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(VALID_USERNAME, INVALID_PASSWORD);
        expectedUserAccessorResponse = createUserModel(VALID_USERNAME, VALID_PASSWORD, false, null, 0);
        mockUserAccessorMethods(false);
        mockAuthenticationResponse(authenticationToken, false);
        long currentAttempts = 0;
        while (currentAttempts < numberOfAttempts) {
            Optional<Authentication> authenticationAttempt = authenticationPerformer.performAuthentication(authenticationToken);
            Assertions.assertTrue(authenticationAttempt.isEmpty());
            currentAttempts++;
        }

        Authentication validAuthenticationToken = new UsernamePasswordAuthenticationToken(VALID_USERNAME, VALID_PASSWORD);
        Optional<Authentication> authenticationAttempt = authenticationPerformer.performAuthentication(validAuthenticationToken);
        Assertions.assertTrue(authenticationAttempt.isEmpty());
        Assertions.assertTrue(expectedUserAccessorResponse.isLocked());
        Assertions.assertEquals(numberOfAttempts, expectedUserAccessorResponse.getFailedLoginAttempts());
    }

    @Test
    void testLockoutDurationExpired() throws AlertForbiddenOperationException, AlertConfigurationException, InterruptedException {
        AlertDatabaseAuthenticationPerformer authenticationPerformer = new AlertDatabaseAuthenticationPerformer(
            authenticationEventManager,
            roleAccessor,
            alertDatabaseAuthProvider,
            userAccessor,
            1,
            1
        );
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(VALID_USERNAME, VALID_PASSWORD);
        expectedUserAccessorResponse = createUserModel(VALID_USERNAME, VALID_PASSWORD, true, OffsetDateTime.now(), 1);
        mockUserAccessorMethods(false);
        mockAuthenticationResponse(authenticationToken, true);
        TimeUnit.SECONDS.sleep(1);
        Optional<Authentication> authentication = authenticationPerformer.performAuthentication(authenticationToken);
        Assertions.assertTrue(authentication.isPresent());
        Assertions.assertFalse(expectedUserAccessorResponse.isLocked());
    }

    @Test
    void testAccessorUpdateException() throws AlertForbiddenOperationException, AlertConfigurationException {
        AlertDatabaseAuthenticationPerformer authenticationPerformer = new AlertDatabaseAuthenticationPerformer(
            authenticationEventManager,
            roleAccessor,
            alertDatabaseAuthProvider,
            userAccessor
        );
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(VALID_USERNAME, VALID_PASSWORD);
        expectedUserAccessorResponse = createUserModel(VALID_USERNAME, VALID_PASSWORD, false, null, 0);
        mockUserAccessorMethods(true);
        mockAuthenticationResponse(authenticationToken, true);
        Optional<Authentication> authentication = authenticationPerformer.performAuthentication(authenticationToken);
        Assertions.assertTrue(authentication.map(Authentication::isAuthenticated).orElseThrow(() -> new AssertionError("User authentication expected but not found")));
    }

    private void mockAuthenticationResponse(Authentication authenticationToken, boolean authenticated) {
        Mockito.doAnswer(invocation -> {
            Authentication response = new TestingAuthenticationToken(VALID_USERNAME, VALID_PASSWORD);
            response.setAuthenticated(authenticated);
            return response;
        }).when(alertDatabaseAuthProvider).authenticate(authenticationToken);
    }

    private void mockUserAccessorMethods(boolean throwException) throws AlertForbiddenOperationException, AlertConfigurationException {
        Mockito.doAnswer(invocation -> {
            if (throwException) {
                throw new AlertException("Expected Accessor exception thrown for testing.");
            }
            expectedUserAccessorResponse = invocation.getArgument(0);
            return expectedUserAccessorResponse;
        }).when(userAccessor).updateUser(Mockito.any(UserModel.class), Mockito.anyBoolean());
        Mockito.doAnswer(invocation -> Optional.of(expectedUserAccessorResponse))
            .when(userAccessor).getUser(VALID_USERNAME);
        Mockito.doAnswer(invocation -> Optional.empty())
            .when(userAccessor).getUser(INVALID_USERNAME);
    }

    private UserModel createUserModel(String username, String password, boolean locked, OffsetDateTime lastFailedLogin, long failedLoginCount) {
        return UserModel.existingUser(
            1L,
            username,
            password,
            String.format("%s@email.example.com", username),
            AuthenticationType.DATABASE,
            Set.of(),
            locked,
            true,
            OffsetDateTime.now(),
            lastFailedLogin,
            failedLoginCount
        );
    }
}
