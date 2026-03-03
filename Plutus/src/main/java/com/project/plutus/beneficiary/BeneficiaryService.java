package com.project.plutus.beneficiary;

import com.project.plutus.beneficiary.model.Beneficiary;

import java.util.UUID;

public interface BeneficiaryService {
    Beneficiary getBeneficiaryEntityById(UUID id);
}
