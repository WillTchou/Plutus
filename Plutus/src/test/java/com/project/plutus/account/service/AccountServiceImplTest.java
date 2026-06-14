package com.project.plutus.account.service;

import com.project.plutus.account.mapper.AccountMapper;
import com.project.plutus.account.model.Account;
import com.project.plutus.account.model.AccountDTO;
import com.project.plutus.account.model.AccountRequest;
import com.project.plutus.account.model.BeneficiaryRequest;
import com.project.plutus.account.repository.AccountRepository;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.beneficiary.repository.BeneficiaryRepository;
import com.project.plutus.exceptions.AccountNotFoundException;
import com.project.plutus.kafka.model.AccountDepositEvent;
import com.project.plutus.kafka.producer.ExternalDepositEventProducer;
import com.project.plutus.user.model.Role;
import com.project.plutus.user.model.User;
import com.project.plutus.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    private static final String EMAIL = "user@plutus.com";
    private static final String IBAN = "FR7630006000011234567890189";

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserService userService;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private ExternalDepositEventProducer externalDepositEventProducer;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void createAccountForUser_createsAccountAndBeneficiary_andPublishesEvent_whenInitialDepositPositive() {
        User user = new User("Jane", "Doe", "1990-01-01", EMAIL, "hashed", Role.ROLE_USER);
        AccountRequest request = AccountRequest.builder()
                .holderName("Jane Doe")
                .iban(IBAN)
                .initialDepositAmount(100)
                .build();

        when(userService.getUserByEmail(EMAIL)).thenReturn(user);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account acc = invocation.getArgument(0);
            acc.setId(UUID.randomUUID());
            return acc;
        });
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.createAccountForUser(EMAIL, request, "idem-key");

        verify(accountRepository).save(any(Account.class));
        verify(beneficiaryRepository).save(any(Beneficiary.class));
        verify(externalDepositEventProducer).sendExternalDepositEvent(any(AccountDepositEvent.class));
    }

    @Test
    void createAccountForUser_doesNotPublishEvent_whenInitialDepositZero() {
        User user = new User("Jane", "Doe", "1990-01-01", EMAIL, "hashed", Role.ROLE_USER);
        AccountRequest request = AccountRequest.builder()
                .holderName("Jane Doe")
                .iban(IBAN)
                .initialDepositAmount(0)
                .build();

        when(userService.getUserByEmail(EMAIL)).thenReturn(user);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(beneficiaryRepository.save(any(Beneficiary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountService.createAccountForUser(EMAIL, request, "idem-key");

        verify(externalDepositEventProducer, never()).sendExternalDepositEvent(any(AccountDepositEvent.class));
    }

    @Test
    void createAccountForUser_throws_whenUserMissing() {
        when(userService.getUserByEmail(EMAIL))
                .thenThrow(new UsernameNotFoundException("User not found"));

        assertThrows(UsernameNotFoundException.class, () ->
                accountService.createAccountForUser(EMAIL, AccountRequest.builder().build(), "idem"));
    }

    @Test
    void getAccountById_returnsDto_whenOwnedByUser() {
        UUID accountId = UUID.randomUUID();
        Account account = Account.builder().id(accountId).build();
        AccountDTO dto = new AccountDTO("Jane Doe", IBAN, 0.0, null, null);

        when(accountRepository.findByIdAndUserEmail(accountId, EMAIL)).thenReturn(Optional.of(account));
        when(accountMapper.toAccountDTO(account)).thenReturn(dto);

        AccountDTO result = accountService.getAccountById(accountId, EMAIL);

        assertEquals(dto, result);
        verify(accountMapper).toAccountDTO(account);
    }

    @Test
    void getAccountById_throws_whenNotFound() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findByIdAndUserEmail(accountId, EMAIL)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccountById(accountId, EMAIL));
    }
}
