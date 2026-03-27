package com.project.plutus.account.controller;

import com.project.plutus.account.model.AccountDTO;
import com.project.plutus.account.model.AccountRequest;
import com.project.plutus.account.model.BeneficiaryRequest;
import com.project.plutus.account.service.AccountService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private static final String EMAIL = "user@plutus.com";
    private static final String IBAN = "FR7630006000011234567890189";

    @Mock
    private AccountService accountService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountController accountController;

    @Test
    void createAccount_returnsCreated() {
        AccountRequest request = AccountRequest.builder()
                .holderName("Jane Doe")
                .iban(IBAN)
                .initialDepositAmount(100)
                .build();
        when(authentication.getName()).thenReturn(EMAIL);
        UUID value = UUID.randomUUID();
        when(accountService.createAccountForUser(EMAIL,request,"idem-key")).thenReturn(value);

        ResponseEntity<UUID> response = accountController.createAccount(request, "idem-key", authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).isNotNull().isEqualTo(value);
    }

    @Test
    void getAccountById_returnsDto() {
        UUID accountId = UUID.randomUUID();
        AccountDTO dto = new AccountDTO("Jane", IBAN, 0.0, null, null);

        when(authentication.getName()).thenReturn(EMAIL);
        when(accountService.getAccountById(accountId, EMAIL)).thenReturn(dto);

        ResponseEntity<AccountDTO> response = accountController.getAccountById(accountId, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
        verify(accountService).getAccountById(accountId, EMAIL);
    }

    @Test
    void addBeneficiary_returnsCreated() {
        UUID accountId = UUID.randomUUID();
        BeneficiaryRequest request = BeneficiaryRequest.builder()
                .holderName("John Beneficiary")
                .iban(IBAN)
                .build();

        when(authentication.getName()).thenReturn(EMAIL);

        ResponseEntity<UUID> response = accountController.addBeneficiary(request, accountId, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertThat(response.getBody()).isNull();
        verify(accountService).addBeneficiary(EMAIL, accountId, request);
    }
}
