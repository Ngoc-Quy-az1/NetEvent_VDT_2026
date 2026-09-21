package com.example.notification.ott.infrastructure.provider;

public interface OttProvider {
    boolean sendOtt(String target, String message);
}
