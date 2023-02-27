package com.synopsys.integration.alert.authentication.ldap.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigTestModel;

@Component
public class LDAPConfigurationValidator {
    public ValidationResponseModel validate(LDAPConfigModel ldapConfigModel) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        if (Boolean.FALSE.equals(ldapConfigModel.getIsManagerPasswordSet())) {
            statuses.add(AlertFieldStatus.error("isManagerPasswordSet", AlertFieldStatusMessages.INVALID_OPTION));
        }
        if (StringUtils.isBlank(ldapConfigModel.getServerName())) {
            statuses.add(AlertFieldStatus.error("serverName", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (StringUtils.isBlank(ldapConfigModel.getManagerDn())) {
            statuses.add(AlertFieldStatus.error("managerDn", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (StringUtils.isBlank(ldapConfigModel.getManagerPassword().orElse("")) && Boolean.FALSE.equals(ldapConfigModel.getIsManagerPasswordSet())) {
            statuses.add(AlertFieldStatus.error("managerPassword", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }

    public ValidationResponseModel validate(LDAPConfigTestModel ldapConfigTestModel) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        LDAPConfigModel ldapConfigModel = ldapConfigTestModel.getLdapConfigModel();

        if (null == ldapConfigModel) {
            statuses.add(AlertFieldStatus.error("ldapConfigModel", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        } else {
            ValidationResponseModel ldapConfigModelValidate = validate(ldapConfigTestModel.getLdapConfigModel());
            statuses.addAll(ldapConfigModelValidate.getErrors().values());
        }
        if (StringUtils.isBlank(ldapConfigTestModel.getTestLDAPUsername())) {
            statuses.add(AlertFieldStatus.error("testLDAPUsername", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (StringUtils.isBlank(ldapConfigTestModel.getTestLDAPPassword())) {
            statuses.add(AlertFieldStatus.error("testLDAPPassword", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }

}
