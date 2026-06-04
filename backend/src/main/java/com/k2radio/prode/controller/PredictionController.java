// src/main/java/com/k2radio/prode/controller/PredictionController.java

package com.k2radio.prode.controller;

import com.k2radio.prode.dto.prediction.*;
import com.k2radio.prode.security.CustomUserDetails;
import com.k2radio.prode.service.PredictionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
@RequiredArgsConstructor
public class PredictionController {

    private final PredictionService predictionService;

    @PostMapping
    public ResponseEntity<PredictionResponse> createOrUpdatePrediction(
            @Valid @RequestBody PredictionRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                predictionService.createOrUpdatePrediction(userDetails.getId(), request)
        );
    }

    @GetMapping
    public ResponseEntity<List<PredictionResponse>> getMyPredictions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                predictionService.getUserPredictions(userDetails.getId())
        );
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<PredictionResponse>> getMyPredictionsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                predictionService.getUserPredictionsPaginated(userDetails.getId(), page, size)
        );
    }

    @GetMapping("/history")
    public ResponseEntity<List<PredictionResponse>> getPredictionHistory(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                predictionService.getUserProcessedPredictions(userDetails.getId(), limit)
        );
    }

    @GetMapping("/pending-count")
    public ResponseEntity<Long> getPendingCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(
                predictionService.getPendingPredictionsCount(userDetails.getId())
        );
    }
}
