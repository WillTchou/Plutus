package com.project.plutus.account.service;

import com.project.plutus.account.mapper.AccountMapper;
import com.project.plutus.account.model.Account;
import com.project.plutus.account.model.AccountDTO;
import com.project.plutus.account.model.AccountRequest;
import com.project.plutus.account.repository.AccountRepository;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.beneficiary.repository.BeneficiaryRepository;
import com.project.plutus.exceptions.AccountNotFoundException;
import com.project.plutus.kafka.model.AccountDepositEvent;
import com.project.plutus.kafka.producer.ExternalDepositEventProducer;
import com.project.plutus.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountMapper accountMapper;
    private final ExternalDepositEventProducer externalDepositEventProducer;

    @Override
    public void createAccountForUser(String userEmail, AccountRequest accountRequest, String idempotencyKey) {
        final var user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        final var account = Account.builder()
                .holderName(accountRequest.getHolderName())
                .iban(accountRequest.getIban())
                .user(user)
                .build();
        accountRepository.save(account);
        final var beneficiary = Beneficiary.builder()
                .account(account)
                .holderName(accountRequest.getHolderName())
                .iban(accountRequest.getIban())
                .build();
        beneficiaryRepository.save(beneficiary);
        publishInitialDepositToTopic(accountRequest, account, beneficiary, idempotencyKey);
    }

    @Override
    public AccountDTO getAccountById(UUID accountId, String userEmail) {
        return accountRepository.findByIdAndUserEmail(accountId, userEmail)
                .map(accountMapper::toAccountDTO)
                .orElseThrow(AccountNotFoundException::new);
    }

    private void publishInitialDepositToTopic(final AccountRequest accountRequest, final Account account,
                                              final Beneficiary beneficiary, final  String idempotencyKey) {
        if (accountRequest.getInitialDepositAmount() > 0) {
            final UUID eventId = UUID.randomUUID();
            final var accountDepositRequest = new AccountDepositEvent(eventId, Instant.now(), account,
                    beneficiary, accountRequest.getInitialDepositAmount(), idempotencyKey);
            externalDepositEventProducer.sendExternalDepositEvent(accountDepositRequest);
        }
    }
}
