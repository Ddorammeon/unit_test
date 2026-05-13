package com.main_project.insurance_service.controller;

import com.main_project.insurance_service.dto.ClaimItemDTO;
import com.main_project.insurance_service.dto.ClaimItemRequestDTO;
import com.main_project.insurance_service.service.IClaimItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/insurance-service/claim-items")
public class ClaimItemController {

    @Autowired
    private IClaimItemService claimItemService;

    @PostMapping
    public ResponseEntity<ClaimItemDTO> createClaimItem(@RequestBody ClaimItemRequestDTO requestDTO) {
        ClaimItemDTO created = claimItemService.createClaimItem(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimItemDTO> getClaimItemById(@PathVariable UUID id) {
        ClaimItemDTO dto = claimItemService.getClaimItemById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<ClaimItemDTO>> getAllClaimItems() {
        List<ClaimItemDTO> list = claimItemService.getAllClaimItems();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/claim/{claimId}")
    public ResponseEntity<List<ClaimItemDTO>> getClaimItemsByClaimId(@PathVariable UUID claimId) {
        List<ClaimItemDTO> list = claimItemService.getClaimItemsByClaimId(claimId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClaimItemDTO> updateClaimItem(@PathVariable UUID id, @RequestBody ClaimItemRequestDTO requestDTO) {
        ClaimItemDTO updated = claimItemService.updateClaimItem(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClaimItem(@PathVariable UUID id) {
        claimItemService.deleteClaimItem(id);
        return ResponseEntity.noContent().build();
    }
}

