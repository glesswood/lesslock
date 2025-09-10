package com.lesswood.lesslock;

import java.io.Closeable;

/** try-with-resources 友好的持锁句柄 */
public interface LockHandle extends Closeable {
    String key();
    String ownerId();

    void unlock();

    @Override
    default void close() { unlock(); }
}