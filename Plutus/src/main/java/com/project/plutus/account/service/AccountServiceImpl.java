package com.project.plutus.account.service;

import com.project.plutus.account.mapper.AccountMapper;
import com.project.plutus.account.model.*;
import com.project.plutus.account.repository.AccountRepository;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.beneficiary.repository.BeneficiaryRepository;
import com.project.plutus.exceptions.AccountNotFoundException;
import com.project.plutus.exceptions.NotEnoughAmountException;
import com.project.plutus.kafka.model.AccountDepositEvent;
import com.project.plutus.kafka.producer.ExternalDepositEventProducer;
import com.project.plutus.user.model.User;
import com.project.plutus.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserService userService;
    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountMapper accountMapper;
    private final ExternalDepositEventProducer externalDepositEventProducer;

    @Override
    public UUID createAccountForUser(String userEmail, AccountRequest accountRequest, String idempotencyKey) {
        final var user = userService.getUserByEmail(userEmail);
        final var account = getAccount(accountRequest, user);
        final var savedAccount = accountRepository.save(account);
        final var beneficiary = getBeneficiary(accountRequest, savedAccount);
        beneficiaryRepository.save(beneficiary);
        Optional.ofNullable(idempotencyKey)
                .filter(not(String::isEmpty))
                .ifPresent(key -> publishInitialDepositToTopic(accountRequest, account, beneficiary, key));
        return savedAccount.getId();
    }

    @Override
    public AccountDTO getAccountById(UUID accountId, String userEmail) {
        return accountRepository.findByIdAndUserEmail(accountId, userEmail)
                .map(accountMapper::toAccountDTO)
                .orElseThrow(AccountNotFoundException::new);
    }

    @Override
    public Account getAccountEntityById(UUID accountId) {
        return accountRepository.findById(accountId).orElseThrow(AccountNotFoundException::new);
    }

    @Override
    public void addBeneficiary(String userEmail, UUID accountId, BeneficiaryRequest beneficiaryRequest) {
        final var account = accountRepository.findByIdAndUserEmail(accountId, userEmail)
                .orElseThrow(AccountNotFoundException::new);
        final var beneficiary = Beneficiary.builder()
                .account(account)
                .holderName(beneficiaryRequest.getHolderName())
                .iban(beneficiaryRequest.getIban())
                .build();
        beneficiaryRepository.save(beneficiary);
    }

    private static Beneficiary getBeneficiary(AccountRequest accountRequest, Account savedAccount) {
        return Beneficiary.builder()
                .account(savedAccount)
                .holderName(accountRequest.getHolderName())
                .iban(accountRequest.getIban())
                .build();
    }

    private static Account getAccount(AccountRequest accountRequest, User user) {
        return Account.builder()
                .holderName(accountRequest.getHolderName())
                .iban(accountRequest.getIban())
                .user(user)
                .status(AccountStatus.ACTIVE)
                .build();
    }

    private void publishInitialDepositToTopic(final AccountRequest accountRequest, final Account account,
                                              final Beneficiary beneficiary, final String idempotencyKey) {
        if (accountRequest.getInitialDepositAmount() > 0) {
            final var accountDepositRequest = new AccountDepositEvent(UUID.randomUUID(), Instant.now(), account.getId(),
                    beneficiary.getId(), accountRequest.getInitialDepositAmount(), idempotencyKey);
            externalDepositEventProducer.sendExternalDepositEvent(accountDepositRequest);
        }
    }
}
