package com.synopsys.integration.alert.authentication.ldap.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;

@Component
public class LDAPConfigurationValidator {
    public ValidationResponseModel validate(LDAPConfigModel ldapConfigModel) {
        Set<AlertFieldStatus> statuses = new HashSet<>();

        if (StringUtils.isBlank(ldapConfigModel.getServerName())) {
            statuses.add(AlertFieldStatus.error("serverName", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (StringUtils.isBlank(ldapConfigModel.getManagerDn())) {
            statuses.add(AlertFieldStatus.error("managerDn", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
      if (ldapConfigModel.getManagerPassword().isEmpty()
            && Boolean.FALSE.equals(ldapConfigModel.getIsManagerPasswordSet())) {
                statuses.add(AlertFieldStatus.error("managerPassword", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }
}
