package com.flatshareteam.flatsharebackend.listings.controller;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.listings.dto.ListingPhotosResponse;
import com.flatshareteam.flatsharebackend.listings.service.ListingPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings/{listingId}/photos")
@RequiredArgsConstructor
public class ListingPhotoController {

    private final ListingPhotoService photoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<Void> addPhoto(
            @PathVariable UUID listingId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {

        UUID photoId = photoService.addPhoto(listingId, user.getId(), file);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{photoId}")
                .buildAndExpand(photoId)
                .toUri();

        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<byte[]> getPhoto(
            @PathVariable UUID listingId,
            @PathVariable UUID photoId) {

        String contentType = photoService.getPhotoContentType(listingId, photoId);
        byte[] data = photoService.getPhotoData(listingId, photoId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(data);
    }

    @GetMapping
    public ResponseEntity<ListingPhotosResponse> listPhotos(
            @PathVariable UUID listingId) {

        return ResponseEntity.ok(photoService.listPhotos(listingId));
    }

    @DeleteMapping("/{photoId}")
    @PreAuthorize("hasRole('LANDLORD')")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable UUID listingId,
            @PathVariable UUID photoId,
            @AuthenticationPrincipal User user) {

        photoService.deletePhoto(listingId, photoId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
