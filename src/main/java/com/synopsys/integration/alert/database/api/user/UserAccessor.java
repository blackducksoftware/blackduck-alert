package com.synopsys.integration.alert.database.api.user;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.relation.UserRoleRelation;
import com.synopsys.integration.alert.database.relation.repository.UserRoleRepository;
import com.synopsys.integration.alert.database.user.RoleRepository;
import com.synopsys.integration.alert.database.user.UserEntity;
import com.synopsys.integration.alert.database.user.UserRepository;

@Component
public class UserAccessor {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Autowired
    public UserAccessor(final UserRepository userRepository, final RoleRepository roleRepository, final UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
    }

    public List<UserModel> getUsers() {
        final List<UserEntity> userList = userRepository.findAll();
        return userList.stream().map(this::createModel).collect(Collectors.toList());
    }

    public Optional<UserModel> getUser(final String username) {
        final Optional<UserModel> userResult;
        final Optional<UserEntity> entity = userRepository.findByUserName(username);
        if (entity.isPresent()) {
            userResult = Optional.ofNullable(createModel(entity.get()));
        } else {
            userResult = Optional.empty();
        }
        return userResult;
    }

    private UserModel createModel(final UserEntity user) {
        final List<UserRoleRelation> roleRelations = userRoleRepository.findAllByUserId(user.getId());
        final List<Long> roleIdsForUser = roleRelations.stream().map(UserRoleRelation::getRoleId).collect(Collectors.toList());
        final List<String> rolesForUser = roleRepository.getRoleNames(roleIdsForUser);
        return UserModel.of(user.getUserName(), user.getPassword(), rolesForUser);
    }
}
