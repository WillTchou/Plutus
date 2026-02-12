package com.project.plutus.account.service;

import com.project.plutus.account.mapper.AccountMapper;
import com.project.plutus.account.model.Account;
import com.project.plutus.account.model.AccountDTO;
import com.project.plutus.account.model.AccountRequest;
import com.project.plutus.account.repository.AccountRepository;
import com.project.plutus.exceptions.AccountNotFoundException;
import com.project.plutus.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    @Override
    public void createAccountForUser(String userEmail, AccountRequest accountRequest) {
        final var user = userRepository.findUserByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        final Account account = Account.builder()
                .holderName(accountRequest.getHolderName())
                .iban(accountRequest.getIban())
                .user(user)
                .build();
        // Create a new transaction for the account with an initial balance
        accountRepository.save(account);
    }

    @Override
    public AccountDTO getAccountById(UUID accountId, String userEmail) {
        return accountRepository.findByIdAndUserEmail(accountId, userEmail)
                .map(accountMapper::toAccountDTO)
                .orElseThrow(AccountNotFoundException::new);
    }
}
