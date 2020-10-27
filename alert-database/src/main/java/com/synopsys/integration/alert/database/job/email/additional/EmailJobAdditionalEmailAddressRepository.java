package com.synopsys.integration.alert.database.job.email.additional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailJobAdditionalEmailAddressRepository extends JpaRepository<EmailJobAdditionalEmailAddressEntity, EmailJobAdditionalEmailAddressPK> {
}
