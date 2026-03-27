package com.project.plutus.beneficiary.repository;

import com.project.plutus.beneficiary.service.BeneficiaryService;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.exceptions.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeneficiaryServiceImpl implements BeneficiaryService {
    private final BeneficiaryRepository beneficiaryRepository;

    @Override
    public Beneficiary getBeneficiaryEntityById(UUID id) {
        return beneficiaryRepository.findById(id).orElseThrow(AccountNotFoundException::new);
    }
}
