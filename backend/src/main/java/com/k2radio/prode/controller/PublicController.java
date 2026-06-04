package com.k2radio.prode.controller;

import com.k2radio.prode.dto.admin.ScoringConfigDTO;
import com.k2radio.prode.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicController {

    private final AdminService adminService;

    @GetMapping("/scoring-config")
    public ResponseEntity<ScoringConfigDTO> getScoringConfig() {
        return ResponseEntity.ok(adminService.getScoringConfig());
    }
}