package com.k2radio.prode.controller;

import com.k2radio.prode.dto.group.GroupDTO;
import com.k2radio.prode.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        return ResponseEntity.ok(groupService.getAllGroups());
    }

    @GetMapping("/{groupName}")
    public ResponseEntity<GroupDTO> getGroupStandings(@PathVariable String groupName) {
        return ResponseEntity.ok(groupService.getGroupStandings(groupName));
    }
}