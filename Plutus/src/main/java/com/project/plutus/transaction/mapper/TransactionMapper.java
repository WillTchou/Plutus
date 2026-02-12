package com.project.plutus.transaction.mapper;

import com.project.plutus.beneficiary.mapper.BeneficiaryMapper;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = BeneficiaryMapper.class)
public interface TransactionMapper {
    @Mapping(source = "transactionType", target = "type")
    TransactionDTO toTransactionDTO(Transaction transaction);
}
