package com.flatshareteam.flatsharebackend.listings.service;

public interface StorageService {
    void upload(String objectKey, byte[] data, String contentType);
    byte[] download(String objectKey);
    void delete(String objectKey);
}
