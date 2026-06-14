package com.project.plutus.beneficiary.controller;

import com.project.plutus.account.model.BeneficiaryRequest;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.beneficiary.model.BeneficiaryDTO;
import com.project.plutus.beneficiary.service.BeneficiaryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {
    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(final BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER') && hasAuthority('VERIFIED')")
    public ResponseEntity<UUID> addBeneficiary(@RequestBody final BeneficiaryRequest beneficiaryRequest,
                                               @RequestHeader UUID accountId,
                                               final Authentication authentication) {
        final String userEmail = authentication.getName();
        beneficiaryService.addBeneficiary(userEmail, accountId, beneficiaryRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') && hasAuthority('VERIFIED')")
    public ResponseEntity<List<BeneficiaryDTO>> getBeneficiaries(@RequestHeader UUID accountId, final Authentication authentication) {
        final String userEmail = authentication.getName();
        return ResponseEntity.ok(beneficiaryService.getBeneficiaries(accountId, userEmail));
    }
}
