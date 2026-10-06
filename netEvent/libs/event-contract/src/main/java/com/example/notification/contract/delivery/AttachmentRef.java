package com.example.notification.contract.delivery;

public class AttachmentRef {
    private String fileName;
    private String storageKey;
    private String contentType;
    private long size;

    public AttachmentRef() { }
    public AttachmentRef(String fileName, String storageKey, String contentType, long size) {
        this.fileName = fileName; this.storageKey = storageKey; this.contentType = contentType; this.size = size;
    }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
}
