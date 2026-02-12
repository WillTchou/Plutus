package com.project.plutus.beneficiary.mapper;

import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.beneficiary.model.BeneficiaryDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BeneficiaryMapper {
    BeneficiaryDTO toBeneficiaryDTO(Beneficiary beneficiary);
}
