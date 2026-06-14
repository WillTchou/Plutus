package com.project.plutus.beneficiary.service;

import com.project.plutus.account.model.Account;
import com.project.plutus.account.model.BeneficiaryRequest;
import com.project.plutus.account.repository.AccountRepository;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.beneficiary.repository.BeneficiaryRepository;
import com.project.plutus.exceptions.AccountNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BeneficiaryServiceImplTest {

    private static final String EMAIL = "user@plutus.com";
    private static final String IBAN = "FR7630006000011234567890189";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @InjectMocks
    private BeneficiaryServiceImpl beneficiaryService;

    @Test
    void getBeneficiaryEntityById_returnsBeneficiary_whenFound() {
        UUID beneficiaryId = UUID.randomUUID();
        Beneficiary beneficiary = Beneficiary.builder().id(beneficiaryId).build();
        when(beneficiaryRepository.findById(beneficiaryId)).thenReturn(Optional.of(beneficiary));

        Beneficiary result = beneficiaryService.getBeneficiaryEntityById(beneficiaryId);

        assertEquals(beneficiary, result);
        verify(beneficiaryRepository).findById(beneficiaryId);
    }

    @Test
    void getBeneficiaryEntityById_throws_whenNotFound() {
        UUID beneficiaryId = UUID.randomUUID();
        when(beneficiaryRepository.findById(beneficiaryId)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> beneficiaryService.getBeneficiaryEntityById(beneficiaryId));
    }

    @Test
    void addBeneficiary_savesBeneficiary_whenAccountOwnedByUser() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).build();
        BeneficiaryRequest request = BeneficiaryRequest.builder()
                .holderName("John Beneficiary")
                .iban(IBAN)
                .build();
        when(accountRepository.findByIdAndUserEmail(accountId, EMAIL)).thenReturn(Optional.of(account));

        beneficiaryService.addBeneficiary(EMAIL, accountId, request);

        ArgumentCaptor<Beneficiary> captor = ArgumentCaptor.forClass(Beneficiary.class);
        verify(beneficiaryRepository).save(captor.capture());
        Beneficiary saved = captor.getValue();
        assertEquals(account, saved.getAccount());
        assertEquals("John Beneficiary", saved.getHolderName());
        assertEquals(IBAN, saved.getIban());
    }

    @Test
    void addBeneficiary_throws_whenAccountMissing() {
        UUID accountId = UUID.randomUUID();
        BeneficiaryRequest request = BeneficiaryRequest.builder()
                .holderName("John Beneficiary")
                .iban(IBAN)
                .build();
        when(accountRepository.findByIdAndUserEmail(accountId, EMAIL)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> beneficiaryService.addBeneficiary(EMAIL, accountId, request));
        verify(beneficiaryRepository, never()).save(any(Beneficiary.class));
    }
}
