package com.lesswood.lesslock.util;

import java.lang.management.ManagementFactory;
import java.util.UUID;

public final class Ids {
    private static final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    public static String newOwnerId() {
        String tid = String.valueOf(Thread.currentThread().getId());
        return PID + ":" + tid + ":" + UUID.randomUUID();
    }
}