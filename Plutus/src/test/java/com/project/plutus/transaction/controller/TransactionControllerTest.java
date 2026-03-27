package com.project.plutus.transaction.controller;

import com.project.plutus.transaction.model.TransactionDTO;
import com.project.plutus.transaction.model.TransactionRequest;
import com.project.plutus.transaction.model.TransactionType;
import com.project.plutus.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void getTransactionById_delegatesToService() {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@plutus.com");
        TransactionDTO dto = new TransactionDTO(100.0, null, "motive", null, TransactionType.DEBIT, null, null);

        when(transactionService.getTransactionById("tx-1", "user@plutus.com")).thenReturn(dto);

        ResponseEntity<TransactionDTO> response = transactionController.getTransactionById("tx-1", authentication);

        assertEquals(dto, response.getBody());
        verify(transactionService).getTransactionById("tx-1", "user@plutus.com");
    }

    @Test
    void getTransactions_delegatesToService() {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@plutus.com");
        UUID accountId = UUID.randomUUID();
        Page<TransactionDTO> page = new PageImpl<>(List.of(
                new TransactionDTO(100.0, null, "motive", null, TransactionType.DEBIT, null, null)
        ));
        PageRequest pageable = PageRequest.of(0, 10);

        when(transactionService.getTransactions(accountId, "user@plutus.com", pageable)).thenReturn(page);

        ResponseEntity<Page<TransactionDTO>> response = transactionController.getTransactions(accountId, pageable, authentication);

        assertEquals(page, response.getBody());
        verify(transactionService).getTransactions(accountId, "user@plutus.com", pageable);
    }

    @Test
    void createTransaction_delegatesToService_andReturnsCreated() {
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authentication.getName()).thenReturn("user@plutus.com");
        UUID accountId = UUID.randomUUID();
        TransactionRequest request = TransactionRequest.builder()
                .amount(120.0)
                .motive("rent")
                .beneficiaryId(UUID.randomUUID())
                .build();

        ResponseEntity<Void> response = transactionController.createTransaction(accountId, "idem-key", request, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(transactionService).createTransaction(accountId, "idem-key", "user@plutus.com", request);
    }
}
