package com.main_project.insurance_service.controller;

import com.main_project.insurance_service.dto.BhytCatalogueDTO;
import com.main_project.insurance_service.dto.BhytCatalogueRequestDTO;
import com.main_project.insurance_service.service.IBhytCatalogueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/insurance-service/bhyt-catalogue")
public class BhytCatalogueController {

    @Autowired
    private IBhytCatalogueService bhytCatalogueService;

    @PostMapping
    public ResponseEntity<BhytCatalogueDTO> createBhytCatalogue(@RequestBody BhytCatalogueRequestDTO requestDTO) {
        BhytCatalogueDTO created = bhytCatalogueService.createBhytCatalogue(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BhytCatalogueDTO> getBhytCatalogueById(@PathVariable UUID id) {
        BhytCatalogueDTO dto = bhytCatalogueService.getBhytCatalogueById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/service-code/{serviceCode}")
    public ResponseEntity<BhytCatalogueDTO> getBhytCatalogueByServiceCode(@PathVariable String serviceCode) {
        BhytCatalogueDTO dto = bhytCatalogueService.getBhytCatalogueByServiceCode(serviceCode);
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<BhytCatalogueDTO>> getAllBhytCatalogues() {
        List<BhytCatalogueDTO> list = bhytCatalogueService.getAllBhytCatalogues();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BhytCatalogueDTO> updateBhytCatalogue(@PathVariable UUID id, @RequestBody BhytCatalogueRequestDTO requestDTO) {
        BhytCatalogueDTO updated = bhytCatalogueService.updateBhytCatalogue(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBhytCatalogue(@PathVariable UUID id) {
        bhytCatalogueService.deleteBhytCatalogue(id);
        return ResponseEntity.noContent().build();
    }
}

