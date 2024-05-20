package com.zigurs.ledger.api;

import com.zigurs.ledger.api.requests.HistoryRequest;
import com.zigurs.ledger.data.AccountsRepository;
import com.zigurs.ledger.data.TransactionsRepository;
import com.zigurs.ledger.model.Account;
import com.zigurs.ledger.model.Client;
import com.zigurs.ledger.model.Transaction;
import com.zigurs.ledger.model.TransactionStatus;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import testutils.Snapshot;

import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.jsonSnapshot.SnapshotMatcher.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static testutils.Utils.asJsonString;

@WebMvcTest(HistoryController.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountsRepository accountsRepository;

    @MockBean
    private TransactionsRepository transactionsRepository;

    @BeforeAll
    public static void beforeAll() {
        start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    void getHistoryHappyPath() throws Exception {
        Client client = new Client(UUID.randomUUID());
        Account account = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        Account differentSourceAccount = new Account(
                UUID.fromString("8c6724de-0879-4392-ba25-cf8dcc79ca36"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(any(UUID.class))).thenReturn(
                Optional.of(account)
        );

        Instant timestamp = Instant.ofEpochSecond(1715933225L);

        when(
                transactionsRepository.findAllBySourceAccountIdOrDestinationAccountIdOrderByTimestampDesc(any(UUID.class), any(UUID.class), any(Pageable.class))
        ).thenReturn(
                List.of(
                        new Transaction(
                                UUID.fromString("424dfead-4625-4dd4-a025-642542eebfdf"),
                                TransactionStatus.COMPLETED,
                                timestamp,
                                account,
                                BigInteger.valueOf(100),
                                BigInteger.valueOf(200),
                                differentSourceAccount,
                                BigInteger.valueOf(300),
                                BigInteger.valueOf(400),
                                "tx one"
                        ),
                        new Transaction(
                                UUID.fromString("ed713c8c-46d1-43b2-9474-63e702983958"),
                                TransactionStatus.COMPLETED,
                                timestamp,
                                differentSourceAccount,
                                BigInteger.valueOf(100),
                                BigInteger.valueOf(200),
                                account,
                                BigInteger.valueOf(300),
                                BigInteger.valueOf(400),
                                "tx two"
                        ),
                        new Transaction(
                                UUID.fromString("5a63b4db-2e30-45bc-a7b7-2dc09f3aaddd"),
                                TransactionStatus.COMPLETED,
                                timestamp,
                                account,
                                BigInteger.valueOf(100),
                                BigInteger.valueOf(200),
                                differentSourceAccount,
                                BigInteger.valueOf(300),
                                BigInteger.valueOf(400),
                                "tx three"
                        ),
                        new Transaction(
                                UUID.fromString("ed49edc4-b1e7-4f89-8bb6-0ef6d917b7da"),
                                TransactionStatus.COMPLETED,
                                timestamp,
                                differentSourceAccount,
                                BigInteger.valueOf(100),
                                BigInteger.valueOf(200),
                                account,
                                BigInteger.valueOf(300),
                                BigInteger.valueOf(400),
                                "tx four"
                        ),
                        new Transaction(
                                UUID.fromString("c6d3cd1f-8fe6-4498-a563-16dbbdaf5bd1"),
                                TransactionStatus.COMPLETED,
                                timestamp,
                                account,
                                BigInteger.valueOf(100),
                                BigInteger.valueOf(200),
                                differentSourceAccount,
                                BigInteger.valueOf(300),
                                BigInteger.valueOf(400),
                                "tx five"
                        )
                )
        );


        HistoryRequest req = new HistoryRequest(UUID.randomUUID(), null, null);

        MvcResult result = mockMvc.perform(
                        post("/history")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void getHistoryInvalidAccount() throws Exception {
        when(accountsRepository.findById(any(UUID.class))).thenReturn(
                Optional.empty()
        );

        HistoryRequest req = new HistoryRequest(UUID.randomUUID(), null, null);

        MvcResult result = mockMvc.perform(
                        post("/history")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }


    @Test
    void getHistoryInvalidOffset() throws Exception {
        Client client = new Client(UUID.randomUUID());
        Account account = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(any(UUID.class))).thenReturn(
                Optional.of(account)
        );

        // TODO - divide by page ID is breaking check on -1
        HistoryRequest req = new HistoryRequest(UUID.randomUUID(), -100, null);

        MvcResult result = mockMvc.perform(
                        post("/history")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }


    @Test
    void getHistoryInvalidLimit() throws Exception {
        Client client = new Client(UUID.randomUUID());
        Account account = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(any(UUID.class))).thenReturn(
                Optional.of(account)
        );

        // TODO - divide by page ID is breaking check on -1
        HistoryRequest req = new HistoryRequest(UUID.randomUUID(), null, 0);

        MvcResult result = mockMvc.perform(
                        post("/history")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }


    @Test
    void getHistoryValidLimitAndOffset() throws Exception {
        Client client = new Client(UUID.randomUUID());
        Account account = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(any(UUID.class))).thenReturn(
                Optional.of(account)
        );

        when(transactionsRepository.findAllBySourceAccountIdOrDestinationAccountIdOrderByTimestampDesc(any(UUID.class), any(UUID.class), any(Pageable.class))).thenReturn(
                List.of()
        );

        HistoryRequest req = new HistoryRequest(UUID.randomUUID(), 100, 100);

        MvcResult result = mockMvc.perform(
                        post("/history")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }
}