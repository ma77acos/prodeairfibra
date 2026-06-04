// src/main/java/com/k2radio/prode/controller/DashboardController.java

package com.k2radio.prode.controller;

import com.k2radio.prode.dto.dashboard.DashboardDTO;
import com.k2radio.prode.security.CustomUserDetails;
import com.k2radio.prode.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDTO> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(dashboardService.getDashboard(userDetails.getId()));
    }
}