package com.flatshareteam.flatsharebackend.accounts.service;

import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesDto;
import com.flatshareteam.flatsharebackend.accounts.dto.UserPreferencesRequest;

import java.util.UUID;

public interface UserPreferencesService {

    UserPreferencesDto getPreferences(UUID userId);

    UserPreferencesDto savePreferences(UUID userId, UserPreferencesRequest request);
}