package com.auth.security;

import com.auth.dto.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/test")
public class TestAdminController {

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-only")
    public ApiResponse<String> adminOnly() {
        return ApiResponse.<String>builder()
                .success(true)
                .data("admin-data")
                .build();
    }
}
