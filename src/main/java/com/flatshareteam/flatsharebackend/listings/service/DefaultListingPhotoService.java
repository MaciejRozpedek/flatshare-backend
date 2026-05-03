package com.flatshareteam.flatsharebackend.listings.service;

import com.flatshareteam.flatsharebackend.listings.dto.ListingPhotosResponse;
import com.flatshareteam.flatsharebackend.listings.model.Listing;
import com.flatshareteam.flatsharebackend.listings.model.ListingPhoto;
import com.flatshareteam.flatsharebackend.listings.repository.ListingPhotoRepository;
import com.flatshareteam.flatsharebackend.listings.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DefaultListingPhotoService implements ListingPhotoService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );

    private final ListingRepository listingRepository;
    private final ListingPhotoRepository listingPhotoRepository;
    private final StorageService storageService;

    @Override
    @Transactional
    public UUID addPhoto(UUID listingId, UUID userId, MultipartFile file) {
        Listing listing = loadOwnedListing(listingId, userId);

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported file type. Allowed: JPEG, PNG, WebP");
        }

        byte[] data;
        try {
            data = file.getBytes();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read uploaded file");
        }

        String photoKey = UUID.randomUUID().toString();
        String objectKey = "listings/" + listingId + "/" + photoKey;

        ListingPhoto photo = ListingPhoto.builder()
                .contentType(contentType)
                .fileSize(file.getSize())
                .objectKey(objectKey)
                .listing(listing)
                .build();

        photo = listingPhotoRepository.save(photo);

        storageService.upload(objectKey, data, contentType);

        return photo.getId();
    }

    @Override
    public byte[] getPhotoData(UUID listingId, UUID photoId) {
        ListingPhoto photo = loadPhoto(listingId, photoId);
        return storageService.download(photo.getObjectKey());
    }

    @Override
    public String getPhotoContentType(UUID listingId, UUID photoId) {
        ListingPhoto photo = loadPhoto(listingId, photoId);
        return photo.getContentType();
    }

    @Override
    public ListingPhotosResponse listPhotos(UUID listingId) {
        if (!listingRepository.existsById(listingId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found");
        }

        List<UUID> photoIds = listingPhotoRepository.findAllByListingId(listingId)
                .stream()
                .map(ListingPhoto::getId)
                .toList();

        return new ListingPhotosResponse(listingId, photoIds);
    }

    @Override
    @Transactional
    public void deletePhoto(UUID listingId, UUID photoId, UUID userId) {
        loadOwnedListing(listingId, userId);
        ListingPhoto photo = loadPhoto(listingId, photoId);

        storageService.delete(photo.getObjectKey());
        listingPhotoRepository.delete(photo);
    }

    private Listing loadOwnedListing(UUID listingId, UUID userId) {
        return listingRepository.findByIdAndLandlordRoleUserId(listingId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Listing not found"));
    }

    private ListingPhoto loadPhoto(UUID listingId, UUID photoId) {
        return listingPhotoRepository.findByIdAndListingId(photoId, listingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Photo not found"));
    }
}
