package com.lesswood.lesslock;

import java.time.Duration;

public record LockConfig(
        String redisHost,
        int redisPort,
        String redisPassword,
        int database,
        Duration lease,
        String keyPrefix,
        boolean autoRenew,
        Duration renewInterval
) {
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String redisHost = "127.0.0.1";
        private int redisPort = 6379;
        private String redisPassword = null;
        private int database = 0;
        private Duration lease = Duration.ofSeconds(30);
        private String keyPrefix = "lock:";
        private boolean autoRenew = true;
        private Duration renewInterval = null; // 默认=lease/3

        public Builder host(String h) { this.redisHost = h; return this; }
        public Builder port(int p) { this.redisPort = p; return this; }
        public Builder password(String pwd) { this.redisPassword = pwd; return this; }
        public Builder database(int db) { this.database = db; return this; }
        public Builder lease(Duration l) { this.lease = l; return this; }
        public Builder keyPrefix(String k) { this.keyPrefix = k; return this; }
        public Builder autoRenew(boolean b) { this.autoRenew = b; return this; }
        public Builder renewInterval(Duration d) { this.renewInterval = d; return this; }

        public LockConfig build() {
            if (renewInterval == null) renewInterval = lease.dividedBy(3);
            return new LockConfig(redisHost, redisPort, redisPassword, database, lease, keyPrefix, autoRenew, renewInterval);
        }
    }
}