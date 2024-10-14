/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.util;

public class BitwiseUtil {
    private BitwiseUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static int shiftBitLeft(int bitShifts) {
        return 1 << bitShifts;
    }

    public static int combineBits(int originalBits, int newBits) {
        return originalBits | newBits;
    }

    public static int removeBits(int originalBits, int oldBits) {
        return ~oldBits & originalBits;
    }

    public static boolean containsBits(int bitContainer, int containedBits) {
        return (containedBits & bitContainer) == containedBits;
    }

}
