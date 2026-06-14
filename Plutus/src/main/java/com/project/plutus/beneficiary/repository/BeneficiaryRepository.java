package com.project.plutus.beneficiary.repository;

import com.project.plutus.beneficiary.model.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.UUID;

public interface BeneficiaryRepository extends JpaRepository<Beneficiary, UUID> {
    @Query("SELECT b FROM Beneficiary b WHERE b.account.id = :accountId AND b.account.user.email = :userEmail")
    List<Beneficiary> findAllByAccountIdAndUserEmail(final UUID accountId, final String userEmail);
}
