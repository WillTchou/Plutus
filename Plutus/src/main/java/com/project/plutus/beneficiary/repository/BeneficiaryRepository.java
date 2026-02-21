package com.project.plutus.beneficiary.repository;

import com.project.plutus.beneficiary.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
}
