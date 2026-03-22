package com.flatshareteam.flatsharebackend.common.utils;

import java.util.UUID;

public final class UuidUtils {

    private UuidUtils() {
    }

    public static String randomUuid() {
        return UUID.randomUUID().toString();
    }
}

