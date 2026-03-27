package com.project.plutus.transaction.mapper;

import com.project.plutus.account.model.Account;
import com.project.plutus.beneficiary.mapper.BeneficiaryMapperImpl;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.beneficiary.model.BeneficiaryDTO;
import com.project.plutus.ledger.model.LedgerEntry;
import com.project.plutus.model.Currency;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionDTO;
import com.project.plutus.transaction.model.TransactionStatus;
import com.project.plutus.transaction.model.TransactionType;
import com.project.plutus.user.model.Role;
import com.project.plutus.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionMapperTest {

    private TransactionMapperImpl transactionMapper;

    @BeforeEach
    void setUp() {
        transactionMapper = new TransactionMapperImpl();
        ReflectionTestUtils.setField(transactionMapper, "beneficiaryMapper", new BeneficiaryMapperImpl());
    }

    @Test
    void toTransactionDTO_mapsFields() {
        Transaction transaction = buildTransaction(TransactionType.DEBIT);
        transaction.setCreatedAt(LocalDateTime.of(2024, 1, 1, 12, 0));
        transaction.setStatus(TransactionStatus.SUCCEEDED);

        TransactionDTO dto = transactionMapper.toTransactionDTO(transaction);

        assertEquals(transaction.getAmount(), dto.amount());
        assertEquals(transaction.getCurrency(), dto.currency());
        assertEquals(transaction.getMotive(), dto.motive());
        assertEquals("2024-01-01T12:00:00", dto.createdAt());
        assertEquals(transaction.getTransactionType(), dto.type());
        assertEquals(transaction.getStatus(), dto.status());
        BeneficiaryDTO beneficiaryDTO = dto.beneficiary();
        assertEquals(transaction.getBeneficiary().getHolderName(), beneficiaryDTO.holderName());
        assertEquals(transaction.getBeneficiary().getIban(), beneficiaryDTO.iban());
    }

    @Test
    void mapAmount_debitAndCredit() {
        Transaction debit = buildTransaction(TransactionType.DEBIT);
        Transaction credit = buildTransaction(TransactionType.CREDIT);

        assertEquals(-debit.getAmount(), transactionMapper.mapAmount(debit));
        assertEquals(credit.getAmount(), transactionMapper.mapAmount(credit));
    }

    @Test
    void mapAccount_debitAndCredit() {
        Transaction debit = buildTransaction(TransactionType.DEBIT);
        Transaction credit = buildTransaction(TransactionType.CREDIT);

        assertEquals(debit.getSourceAccount(), transactionMapper.mapAccount(debit));
        assertEquals(credit.getBeneficiary().getAccount(), transactionMapper.mapAccount(credit));
    }

    @Test
    void toLedgerEntry_usesMappedFields() {
        Transaction transaction = buildTransaction(TransactionType.DEBIT);

        LedgerEntry entry = transactionMapper.toLedgerEntry(transaction);

        assertEquals(-transaction.getAmount(), entry.getAmount());
        assertEquals(Currency.EUR, entry.getCurrency());
        assertEquals(TransactionType.DEBIT, entry.getTransactionType());
        assertEquals(transaction.getSourceAccount(), entry.getAccount());
    }

    private static Transaction buildTransaction(TransactionType type) {
        User sourceUser = new User("Jane", "Doe", "1990-01-01", "source@plutus.com", "pw", Role.ROLE_USER);
        User beneficiaryUser = new User("Ben", "Eficiary", "1990-01-01", "beneficiary@plutus.com", "pw", Role.ROLE_USER);
        Account sourceAccount = Account.builder().id(UUID.randomUUID()).holderName("Jane Doe").iban("FR7630006000011234567890189").user(sourceUser).build();
        Account beneficiaryAccount = Account.builder().id(UUID.randomUUID()).holderName("Ben Eficiary").iban("FR7630006000011234567890190").user(beneficiaryUser).build();
        Beneficiary beneficiary = Beneficiary.builder().id(UUID.randomUUID()).holderName("Ben Eficiary").iban("FR7630006000011234567890190")
                .account(beneficiaryAccount).build();

        return Transaction.builder()
                .id("tx-1")
                .amount(100.0)
                .currency(Currency.EUR)
                .motive("transfer")
                .transactionType(type)
                .status(TransactionStatus.PENDING)
                .sourceAccount(sourceAccount)
                .beneficiary(beneficiary)
                .build();
    }
}
