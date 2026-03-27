package com.project.plutus.transaction.service;

import com.project.plutus.account.model.Account;
import com.project.plutus.account.repository.AccountRepository;
import com.project.plutus.beneficiary.model.Beneficiary;
import com.project.plutus.beneficiary.service.BeneficiaryService;
import com.project.plutus.exceptions.AccountNotFoundException;
import com.project.plutus.exceptions.TransactionNotFoundException;
import com.project.plutus.kafka.model.PaymentEvent;
import com.project.plutus.kafka.producer.PaymentProcessorEventProducer;
import com.project.plutus.transaction.mapper.TransactionMapper;
import com.project.plutus.transaction.model.Transaction;
import com.project.plutus.transaction.model.TransactionDTO;
import com.project.plutus.transaction.model.TransactionRequest;
import com.project.plutus.transaction.model.TransactionType;
import com.project.plutus.transaction.repository.TransactionRepository;
import com.project.plutus.user.model.Role;
import com.project.plutus.user.model.User;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    private static final String EMAIL = "user@plutus.com";
    private static final String TX_1 = "tx-1";
    private static final String MOTIVE = "motive";

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BeneficiaryService beneficiaryService;

    @Mock
    private PaymentProcessorEventProducer paymentProcessorEventProducer;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void getTransactionById_returnsDto_whenUserIsOwner() {
        Transaction transaction = buildTransaction(EMAIL, TransactionType.DEBIT);
        TransactionDTO dto = new TransactionDTO(100.0, null, MOTIVE, null, TransactionType.DEBIT, null, null);

        when(transactionRepository.findById(TX_1)).thenReturn(Optional.of(transaction));
        when(transactionMapper.toTransactionDTO(transaction)).thenReturn(dto);

        TransactionDTO result = transactionService.getTransactionById(TX_1, EMAIL);

        assertEquals(dto, result);
        verify(transactionMapper).toTransactionDTO(transaction);
    }

    @Test
    void getTransactionById_throws_whenNotFound() {
        final String missingId = "missing";
        when(transactionRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(missingId, EMAIL));
    }

    @Test
    void getTransactionById_throws_whenNotOwner() {
        Transaction transaction = buildTransaction("source@plutus.com", TransactionType.DEBIT);
        when(transactionRepository.findById(TX_1)).thenReturn(Optional.of(transaction));

        assertThrows(TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(TX_1, "other@plutus.com"));
    }

    @Test
    void getTransactions_returnsPage_whenValid() {
        Transaction tx1 = buildTransaction(EMAIL, TransactionType.DEBIT);
        Transaction tx2 = buildTransaction(EMAIL, TransactionType.CREDIT);
        TransactionDTO dto1 = new TransactionDTO(100.0, null, MOTIVE, null, TransactionType.DEBIT, null, null);

        when(transactionRepository.findAllBySourceAccountIdOrBeneficiaryAccountId(any(UUID.class)))
                .thenReturn(List.of(tx1, tx2));
        when(transactionMapper.toTransactionDTO(tx1)).thenReturn(dto1);

        Page<TransactionDTO> page = transactionService.getTransactions(UUID.randomUUID(), EMAIL, PageRequest.of(0, 1));

        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
        assertEquals(dto1, page.getContent().get(0));
    }

    @Test
    void getTransactions_throws_whenInvalidPagination() {
        assertThrows(IndexOutOfBoundsException.class, () ->
                transactionService.getTransactions(UUID.randomUUID(), EMAIL, PageRequest.of(2, 1)));
    }

    @Test
    void createTransaction_sendsPaymentEvent_whenValid() {
        UUID accountId = UUID.randomUUID();
        UUID beneficiaryId = UUID.randomUUID();
        TransactionRequest request = TransactionRequest.builder()
                .amount(250.0)
                .motive("rent")
                .beneficiaryId(beneficiaryId)
                .build();

        Account account = Account.builder()
                .id(accountId)
                .user(new User("Jane", "Doe", "1990-01-01", EMAIL, "pw", Role.ROLE_USER))
                .build();
        Beneficiary beneficiary = Beneficiary.builder()
                .id(beneficiaryId)
                .account(Account.builder().id(UUID.randomUUID()).build())
                .build();

        when(accountRepository.findByIdAndUserEmail(accountId, EMAIL)).thenReturn(Optional.of(account));
        when(beneficiaryService.getBeneficiaryEntityById(beneficiaryId)).thenReturn(beneficiary);

        final String idempotencyKey = "idem-key";
        transactionService.createTransaction(accountId, idempotencyKey, EMAIL, request);

        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        verify(paymentProcessorEventProducer).sendPaymentEvent(captor.capture());
        PaymentEvent event = captor.getValue();
        assertEquals(accountId, event.sourceAccountId());
        assertEquals(beneficiaryId, event.beneficiaryId());
        assertEquals("rent", event.motive());
        assertEquals(250.0, event.amount());
        assertEquals(idempotencyKey, event.idempotencyKey());
    }

    @Test
    void createTransaction_throws_whenAccountMissing() {
        final UUID accountId = UUID.randomUUID();
        when(accountRepository.findByIdAndUserEmail(accountId, EMAIL)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () ->
                transactionService.createTransaction(accountId, "idem", EMAIL,
                        TransactionRequest.builder().amount(10).motive(MOTIVE).beneficiaryId(UUID.randomUUID()).build()));
    }

    private static Transaction buildTransaction(String sourceEmail, TransactionType type) {
        User sourceUser = new User("Jane", "Doe", "1990-01-01", sourceEmail, "pw", Role.ROLE_USER);
        User beneficiaryUser = new User("Ben", "Eficiary", "1990-01-01", "beneficiary@plutus.com", "pw", Role.ROLE_USER);
        Account sourceAccount = Account.builder().id(UUID.randomUUID()).user(sourceUser).build();
        Account beneficiaryAccount = Account.builder().id(UUID.randomUUID()).user(beneficiaryUser).build();
        Beneficiary beneficiary = Beneficiary.builder().id(UUID.randomUUID()).account(beneficiaryAccount).build();
        return Transaction.builder()
                .id(TX_1)
                .amount(100.0)
                .motive(MOTIVE)
                .transactionType(type)
                .sourceAccount(sourceAccount)
                .beneficiary(beneficiary)
                .build();
    }
}
