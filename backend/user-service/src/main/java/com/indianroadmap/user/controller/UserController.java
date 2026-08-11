package com.indianroadmap.user.controller;

import com.indianroadmap.user.dto.request.UpdatePreferencesRequest;
import com.indianroadmap.user.dto.request.UpdateProfileRequest;
import com.indianroadmap.user.dto.response.ApiResponse;
import com.indianroadmap.user.dto.response.UserPreferencesResponse;
import com.indianroadmap.user.dto.response.UserResponse;
import com.indianroadmap.user.exception.InvalidTokenException;
import com.indianroadmap.user.security.UserPrincipal;
import com.indianroadmap.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.getCurrentUser(currentUserId(principal)));
    }

    @PutMapping("/me")
    public ApiResponse<UserResponse> updateMe(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody UpdateProfileRequest request) {
        return ApiResponse.ok(userService.updateCurrentUser(currentUserId(principal), request));
    }

    @GetMapping("/me/preferences")
    public ApiResponse<UserPreferencesResponse> getPreferences(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.ok(userService.getPreferences(currentUserId(principal)));
    }

    @PutMapping("/me/preferences")
    public ApiResponse<UserPreferencesResponse> updatePreferences(@AuthenticationPrincipal UserPrincipal principal,
                                                                  @Valid @RequestBody UpdatePreferencesRequest request) {
        return ApiResponse.ok(userService.updatePreferences(currentUserId(principal), request));
    }

    private String currentUserId(UserPrincipal principal) {
        if (principal != null) {
            return principal.userId();
        }

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Object candidate = authentication.getPrincipal();
            if (candidate instanceof UserPrincipal userPrincipal) {
                return userPrincipal.userId();
            }
            if (candidate instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            }
            if (candidate instanceof String userId && !userId.isBlank() && !"anonymousUser".equals(userId)) {
                return userId;
            }
        }

        throw new InvalidTokenException("Authenticated user principal is missing");
    }
}
