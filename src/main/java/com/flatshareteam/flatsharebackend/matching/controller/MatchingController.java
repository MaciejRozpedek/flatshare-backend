package com.flatshareteam.flatsharebackend.matching.controller;

import com.flatshareteam.flatsharebackend.accounts.model.User;
import com.flatshareteam.flatsharebackend.matching.dto.MatchFilterCriteria;
import com.flatshareteam.flatsharebackend.matching.dto.MatchResult;
import com.flatshareteam.flatsharebackend.matching.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping
    @PreAuthorize("hasRole('TENANT')")
    public ResponseEntity<Page<MatchResult>> getMatches(
            @AuthenticationPrincipal User user,
            @ParameterObject MatchFilterCriteria filter,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(matchingService.getMatches(user.getId(), filter, pageable));
    }
}
