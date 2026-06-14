package com.project.plutus.beneficiary.service;

import com.project.plutus.account.model.BeneficiaryRequest;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.beneficiary.model.BeneficiaryDTO;

import java.util.List;
import java.util.UUID;

public interface BeneficiaryService {
    Beneficiary getBeneficiaryEntityById(UUID id);
    void addBeneficiary(String userEmail, UUID accountId, BeneficiaryRequest beneficiaryRequest);
    List<BeneficiaryDTO> getBeneficiaries(UUID accountId, String userEmail);
}
