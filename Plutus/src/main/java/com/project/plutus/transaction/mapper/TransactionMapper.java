package com.project.plutus.transaction.mapper;

import com.project.plutus.account.model.Account;
import com.project.plutus.beneficiary.mapper.BeneficiaryMapper;
import com.project.plutus.ledger.model.LedgerEntry;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = BeneficiaryMapper.class)
public interface TransactionMapper {
    @Mapping(source = "transactionType", target = "type")
    TransactionDTO toTransactionDTO(Transaction transaction);

    @Named("mapAmount")
    default Double mapAmount(final Transaction transaction) {
        Double amount = transaction.getAmount();
        if (amount == null) {
            return null;
        }
        switch (transaction.getTransactionType()) {
            case DEBIT -> {
                return -amount;
            }
            case CREDIT -> {
                return amount;
            }
            default -> throw new IllegalArgumentException("Unknown transaction type: " + transaction.getTransactionType());
        }
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "transaction", target = "account", qualifiedByName = "mapAccount")
    @Mapping(source = "transaction", target = "amount", qualifiedByName = "mapAmount")
    LedgerEntry toLedgerEntry(Transaction transaction);

    @Named("mapAccount")
    default Account mapAccount(final Transaction transaction) {
        switch (transaction.getTransactionType()) {
            case DEBIT -> {
                return transaction.getSourceAccount();
            }
            case CREDIT -> {
                return transaction.getBeneficiary().getAccount();
            }
            default -> throw new IllegalArgumentException("Unknown transaction type: " + transaction.getTransactionType());
        }
    }
}
