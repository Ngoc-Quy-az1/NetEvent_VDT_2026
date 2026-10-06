package com.example.notification.ott.infrastructure.provider;

public interface OttProvider {
    boolean sendOtt(String target, String message);

    boolean sendDocument(String target, String fileName, byte[] content, String contentType, String caption);
}
