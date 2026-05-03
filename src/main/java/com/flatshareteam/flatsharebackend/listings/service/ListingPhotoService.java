package com.flatshareteam.flatsharebackend.listings.service;

import com.flatshareteam.flatsharebackend.listings.dto.ListingPhotosResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface ListingPhotoService {
    UUID addPhoto(UUID listingId, UUID userId, MultipartFile file);
    byte[] getPhotoData(UUID listingId, UUID photoId);
    String getPhotoContentType(UUID listingId, UUID photoId);
    ListingPhotosResponse listPhotos(UUID listingId);
    void deletePhoto(UUID listingId, UUID photoId, UUID userId);
}
