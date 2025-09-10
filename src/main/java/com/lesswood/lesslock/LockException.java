package com.lesswood.lesslock;

public class LockException extends RuntimeException {
    public LockException(String msg) { super(msg); }
    public LockException(String msg, Throwable cause) { super(msg, cause); }
}