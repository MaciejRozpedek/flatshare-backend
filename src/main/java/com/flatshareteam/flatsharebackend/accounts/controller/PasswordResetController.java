package com.flatshareteam.flatsharebackend.accounts.controller;

import com.flatshareteam.flatsharebackend.accounts.dto.MessageResponse;
import com.flatshareteam.flatsharebackend.accounts.dto.PasswordResetConfirmRequest;
import com.flatshareteam.flatsharebackend.accounts.dto.PasswordResetRequestRequest;
import com.flatshareteam.flatsharebackend.accounts.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/request")
    public ResponseEntity<MessageResponse> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestRequest request
    ) {
        passwordResetService.requestPasswordReset(request.email());

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(new MessageResponse(
                        "If the account exists, password reset instructions have been sent."
                ));
    }

    @PostMapping("/confirm")
    public ResponseEntity<MessageResponse> confirmPasswordReset(
            @Valid @RequestBody PasswordResetConfirmRequest request
    ) {
        passwordResetService.confirmPasswordReset(
                request.resetToken(),
                request.newPassword()
        );

        return ResponseEntity
                .ok(new MessageResponse("Password has been changed successfully."));
    }
}