package com.project.plutus.beneficiary.service;

import com.project.plutus.account.model.BeneficiaryRequest;
import com.project.plutus.account.repository.AccountRepository;
import com.project.plutus.beneficiary.mapper.BeneficiaryMapper;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.beneficiary.model.BeneficiaryDTO;
import com.project.plutus.beneficiary.repository.BeneficiaryRepository;
import com.project.plutus.exceptions.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {
    private final AccountRepository accountRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final BeneficiaryMapper beneficiaryMapper;

    @Override
    public Beneficiary getBeneficiaryEntityById(UUID id) {
        return beneficiaryRepository.findById(id).orElseThrow(AccountNotFoundException::new);
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

    @Override
    public List<BeneficiaryDTO> getBeneficiaries(UUID accountId, String userEmail) {
        return beneficiaryRepository.findAllByAccountIdAndUserEmail(accountId, userEmail).stream()
                .map(beneficiaryMapper::toBeneficiaryDTO)
                .toList();
    }
}
