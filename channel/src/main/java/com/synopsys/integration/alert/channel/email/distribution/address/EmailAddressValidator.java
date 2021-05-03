/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.distribution.address;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class EmailAddressValidator {

    // TODO rename model?
    public ValidatedEmailAddresses validate(Collection<String> emailAddresses) {
        // FIXME implement
        return new ValidatedEmailAddresses(new HashSet<>(emailAddresses), Set.of());
    }

}
