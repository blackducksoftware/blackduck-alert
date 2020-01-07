package com.synopsys.integration.alert.database.api;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.AuthenticationTypeAccessor;
import com.synopsys.integration.alert.common.persistence.model.AuthenticationType;
import com.synopsys.integration.alert.database.user.AuthenticationTypeEntity;
import com.synopsys.integration.alert.database.user.AuthenticationTypeRepository;

@Component
@Transactional
public class DefaultAuthenticationTypeAccessor implements AuthenticationTypeAccessor {
    private final AuthenticationTypeRepository authenticationTypeRepository;

    @Autowired
    public DefaultAuthenticationTypeAccessor(AuthenticationTypeRepository authenticationTypeRepository) {
        this.authenticationTypeRepository = authenticationTypeRepository;
    }

    @Override
    public Optional<AuthenticationType> getAuthenticationType(Long id) {
        Optional<AuthenticationTypeEntity> authenticationTypeEntity = authenticationTypeRepository.findById(id);
        return authenticationTypeEntity.map(entity -> new AuthenticationType(entity.getId(), entity.getName()));
    }
}
