package com.k2radio.prode.controller;

import com.k2radio.prode.dto.admin.GroupStandingDTO;
import com.k2radio.prode.service.GroupStandingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/standings")
@RequiredArgsConstructor
public class StandingsController {

    private final GroupStandingsService groupStandingsService;

    @GetMapping
    public ResponseEntity<Map<String, List<GroupStandingDTO>>> getAllStandings() {
        return ResponseEntity.ok(groupStandingsService.getAllGroupStandings());
    }

    @GetMapping("/{group}")
    public ResponseEntity<List<GroupStandingDTO>> getGroupStandings(@PathVariable String group) {
        return ResponseEntity.ok(groupStandingsService.getGroupStandings(group.toUpperCase()));
    }
}