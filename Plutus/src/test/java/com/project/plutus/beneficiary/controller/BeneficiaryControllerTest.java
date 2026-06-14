package com.project.plutus.beneficiary.controller;

import com.project.plutus.account.model.BeneficiaryRequest;
import com.project.plutus.beneficiary.service.BeneficiaryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiaryControllerTest {

    private static final String EMAIL = "user@plutus.com";
    private static final String IBAN = "FR7630006000011234567890189";

    @Mock
    private BeneficiaryService beneficiaryService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BeneficiaryController beneficiaryController;

    @Test
    void addBeneficiary_returnsCreated() {
        UUID accountId = UUID.randomUUID();
        BeneficiaryRequest request = BeneficiaryRequest.builder()
                .holderName("John Beneficiary")
                .iban(IBAN)
                .build();
        when(authentication.getName()).thenReturn(EMAIL);

        ResponseEntity<UUID> response = beneficiaryController.addBeneficiary(request, accountId, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(beneficiaryService).addBeneficiary(EMAIL, accountId, request);
    }
}
