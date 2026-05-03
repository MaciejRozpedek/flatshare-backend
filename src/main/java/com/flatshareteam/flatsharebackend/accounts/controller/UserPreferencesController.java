package com.flatshareteam.flatsharebackend.accounts.controller;

import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesDto;
import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesRequest;
import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.accounts.service.UserPreferencesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/v1/users/me/preferences")
@RequiredArgsConstructor
@PreAuthorize("hasRole('TENANT')")
public class UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

    @GetMapping
    public ResponseEntity<UserPreferencesDto> getPreferences(@AuthenticationPrincipal User authenticatedUser) {
        return ResponseEntity.ok(userPreferencesService.getPreferences(authenticatedUser.getId()));
    }

    @PutMapping
    public ResponseEntity<UserPreferencesDto> savePreferences(
            @AuthenticationPrincipal User authenticatedUser,
            @Valid @RequestBody UserPreferencesRequest request) {

        UserPreferencesDto response = userPreferencesService.savePreferences(authenticatedUser.getId(), request);
        return ResponseEntity.ok(response);
    }
}