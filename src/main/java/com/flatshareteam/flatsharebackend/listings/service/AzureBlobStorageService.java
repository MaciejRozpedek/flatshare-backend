package com.flatshareteam.flatsharebackend.listings.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class AzureBlobStorageService implements StorageService {

    private final BlobContainerClient containerClient;

    @Override
    public void upload(String objectKey, byte[] data, String contentType) {
        BlobClient blobClient = containerClient.getBlobClient(objectKey);
        blobClient.upload(new ByteArrayInputStream(data), data.length, true);
        blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType(contentType));
    }

    @Override
    public byte[] download(String objectKey) {
        BlobClient blobClient = containerClient.getBlobClient(objectKey);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.downloadStream(outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public void delete(String objectKey) {
        BlobClient blobClient = containerClient.getBlobClient(objectKey);
        blobClient.deleteIfExists();
    }
}
