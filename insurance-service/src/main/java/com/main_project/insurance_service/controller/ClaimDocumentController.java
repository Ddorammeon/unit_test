package com.main_project.insurance_service.controller;

import com.main_project.insurance_service.dto.ClaimDocumentDTO;
import com.main_project.insurance_service.dto.ClaimDocumentRequestDTO;
import com.main_project.insurance_service.service.IClaimDocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/insurance-service/claim-documents")
@RequiredArgsConstructor
@Tag(name = "Claim Document", description = "Claim Document Management APIs")
public class ClaimDocumentController {

    private final IClaimDocumentService claimDocumentService;

    @GetMapping
    @Operation(summary = "Get all claim documents", description = "Retrieve all claim document records")
    public ResponseEntity<List<ClaimDocumentDTO>> getAllDocuments() {
        List<ClaimDocumentDTO> documents = claimDocumentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get claim document by ID", description = "Retrieve a specific claim document by its ID")
    public ResponseEntity<ClaimDocumentDTO> getDocumentById(
            @Parameter(description = "Document ID") @PathVariable UUID id) {
        Optional<ClaimDocumentDTO> document = claimDocumentService.getDocumentById(id);
        return document.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/claim/{claimId}")
    @Operation(summary = "Get documents by claim ID", description = "Retrieve all documents for a specific insurance claim")
    public ResponseEntity<List<ClaimDocumentDTO>> getDocumentsByClaimId(
            @Parameter(description = "Insurance Claim ID") @PathVariable UUID claimId) {
        List<ClaimDocumentDTO> documents = claimDocumentService.getDocumentsByClaimId(claimId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/type/{documentType}")
    @Operation(summary = "Get documents by document type", description = "Retrieve claim documents by document type")
    public ResponseEntity<List<ClaimDocumentDTO>> getDocumentsByDocumentType(
            @Parameter(description = "Document Type") @PathVariable String documentType) {
        List<ClaimDocumentDTO> documents = claimDocumentService.getDocumentsByDocumentType(documentType);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get documents by status", description = "Retrieve claim documents by status")
    public ResponseEntity<List<ClaimDocumentDTO>> getDocumentsByStatus(
            @Parameter(description = "Document Status") @PathVariable String status) {
        List<ClaimDocumentDTO> documents = claimDocumentService.getDocumentsByStatus(status);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/search")
    @Operation(summary = "Search documents by claim ID and type", description = "Search claim documents by claim ID and document type")
    public ResponseEntity<List<ClaimDocumentDTO>> getDocumentsByClaimIdAndType(
            @Parameter(description = "Insurance Claim ID") @RequestParam UUID claimId,
            @Parameter(description = "Document Type") @RequestParam String documentType) {
        List<ClaimDocumentDTO> documents = claimDocumentService.getDocumentsByClaimIdAndType(claimId, documentType);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get documents by patient ID", description = "Retrieve all claim documents for a specific patient")
    public ResponseEntity<List<ClaimDocumentDTO>> getDocumentsByPatientId(
            @Parameter(description = "Patient ID") @PathVariable UUID patientId) {
        List<ClaimDocumentDTO> documents = claimDocumentService.getDocumentsByPatientId(patientId);
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/count/claim/{claimId}")
    @Operation(summary = "Get document count by claim ID", description = "Get the number of documents for a specific claim")
    public ResponseEntity<Long> getDocumentCountByClaimId(
            @Parameter(description = "Insurance Claim ID") @PathVariable UUID claimId) {
        long count = claimDocumentService.getDocumentCountByClaimId(claimId);
        return ResponseEntity.ok(count);
    }

    @PostMapping
    @Operation(summary = "Create new claim document", description = "Create a new claim document record")
    public ResponseEntity<ClaimDocumentDTO> createDocument(
            @Valid @RequestBody ClaimDocumentRequestDTO requestDTO) {
        try {
            ClaimDocumentDTO createdDocument = claimDocumentService.createDocument(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDocument);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update claim document", description = "Update an existing claim document")
    public ResponseEntity<ClaimDocumentDTO> updateDocument(
            @Parameter(description = "Document ID") @PathVariable UUID id,
            @Valid @RequestBody ClaimDocumentRequestDTO requestDTO) {
        try {
            ClaimDocumentDTO updatedDocument = claimDocumentService.updateDocument(id, requestDTO);
            return ResponseEntity.ok(updatedDocument);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update document status", description = "Update the status of a claim document")
    public ResponseEntity<ClaimDocumentDTO> updateDocumentStatus(
            @Parameter(description = "Document ID") @PathVariable UUID id,
            @Parameter(description = "New Status") @RequestParam String status) {
        try {
            ClaimDocumentDTO updatedDocument = claimDocumentService.updateDocumentStatus(id, status);
            return ResponseEntity.ok(updatedDocument);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete claim document", description = "Delete a claim document")
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "Document ID") @PathVariable UUID id) {
        try {
            claimDocumentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/claim/{claimId}")
    @Operation(summary = "Delete all documents by claim ID", description = "Delete all documents for a specific insurance claim")
    public ResponseEntity<Void> deleteDocumentsByClaimId(
            @Parameter(description = "Insurance Claim ID") @PathVariable UUID claimId) {
        claimDocumentService.deleteDocumentsByClaimId(claimId);
        return ResponseEntity.noContent().build();
    }
}





