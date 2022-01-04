/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.distribution.address;

import java.util.Set;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class ValidatedEmailAddresses extends AlertSerializableModel {
    private final Set<String> validEmailAddresses;
    private final Set<String> invalidEmailAddresses;

    public ValidatedEmailAddresses(Set<String> validEmailAddresses, Set<String> invalidEmailAddresses) {
        this.validEmailAddresses = validEmailAddresses;
        this.invalidEmailAddresses = invalidEmailAddresses;
    }

    public Set<String> getValidEmailAddresses() {
        return validEmailAddresses;
    }

    public Set<String> getInvalidEmailAddresses() {
        return invalidEmailAddresses;
    }

}
