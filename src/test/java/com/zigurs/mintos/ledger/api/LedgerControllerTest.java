package com.zigurs.mintos.ledger.api;

import com.zigurs.mintos.ledger.api.requests.AccountsRequest;
import com.zigurs.mintos.ledger.api.requests.HistoryRequest;
import com.zigurs.mintos.ledger.api.requests.TransferRequest;
import com.zigurs.mintos.ledger.data.AccountsRepository;
import com.zigurs.mintos.ledger.data.ClientsRepository;
import com.zigurs.mintos.ledger.data.TransactionsRepository;
import com.zigurs.mintos.ledger.model.Account;
import com.zigurs.mintos.ledger.model.Client;
import com.zigurs.mintos.ledger.model.Transaction;
import com.zigurs.mintos.ledger.model.TransactionStatus;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.config.SortHandlerMethodArgumentResolverCustomizer;
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
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static testutils.Utils.asJsonString;

@WebMvcTest(LedgerController.class)
class LedgerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientsRepository clientsRepository;

    @MockBean
    private AccountsRepository accountsRepository;

    @MockBean
    private TransactionsRepository transactionsRepository;

    @MockBean
    private FXConverterService fxConverterService;

    @MockBean
    private TransferService transferService;
    @Autowired
    private SortHandlerMethodArgumentResolverCustomizer sortCustomizer;

    @BeforeAll
    public static void beforeAll() {
        start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    void getAccountsHappyPath() throws Exception {
        Client client = new Client(UUID.randomUUID());
        Account account = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(clientsRepository.findById(any(UUID.class))).thenReturn(
                Optional.of(client)
        );

        when(accountsRepository.findByClientId(any(UUID.class))).thenReturn(
                List.of(account)
        );

        AccountsRequest req = new AccountsRequest(client.getId());

        MvcResult result = mockMvc.perform(
                        post("/accounts")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(account.getId().toString())))
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void getAccountsEmptyList() throws Exception {
        Client client = new Client(UUID.randomUUID());

        when(clientsRepository.findById(any(UUID.class))).thenReturn(
                Optional.of(client)
        );

        when(accountsRepository.findByClientId(any(UUID.class))).thenReturn(
                List.of()
        );

        AccountsRequest req = new AccountsRequest(UUID.randomUUID());

        MvcResult result = mockMvc.perform(
                        post("/accounts")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void getAccountsInvalidClient() throws Exception {
        when(clientsRepository.findById(any(UUID.class))).thenReturn(
                Optional.empty()
        );

        AccountsRequest req = new AccountsRequest(UUID.randomUUID());

        MvcResult result = mockMvc.perform(
                        post("/accounts")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void getAccountsWhenFetchingClient() throws Exception {
        when(clientsRepository.findById(any(UUID.class))).thenThrow(
                new RuntimeException()
        );

        AccountsRequest req = new AccountsRequest(UUID.randomUUID());

        MvcResult result = mockMvc.perform(
                        post("/accounts")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is5xxServerError())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void getAccountsWhenFetchingAccounts() throws Exception {
        Client client = new Client(UUID.randomUUID());

        when(clientsRepository.findById(any(UUID.class))).thenReturn(
                Optional.of(client)
        );

        when(accountsRepository.findByClientId(any(UUID.class))).thenThrow(
                new RuntimeException()
        );

        AccountsRequest req = new AccountsRequest(UUID.randomUUID());

        MvcResult result = mockMvc.perform(
                        post("/accounts")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is5xxServerError())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
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
    void getHistoryMissingAccount() throws Exception {
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

    @Test
    void transferHappyPath() throws Exception {
        Client client = new Client(UUID.randomUUID());

        Account sourceAccount = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        Account destinationAccount = new Account(
                UUID.fromString("1ea6d532-faca-4240-aacf-0356b1e0e950"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(sourceAccount.getId())).thenReturn(
                Optional.of(sourceAccount)
        );

        when(accountsRepository.findById(destinationAccount.getId())).thenReturn(
                Optional.of(destinationAccount)
        );

        Instant timestamp = Instant.ofEpochSecond(1715933225L);

        when(transferService.transfer(
                any(),
                eq(sourceAccount.getId()),
                eq(BigInteger.valueOf(50)),
                eq(destinationAccount.getId()),
                eq(BigInteger.valueOf(50)),
                anyString()
        )).thenReturn(
                new Transaction(
                        UUID.fromString("424dfead-4625-4dd4-a025-642542eebfdf"),
                        TransactionStatus.COMPLETED,
                        timestamp,
                        sourceAccount,
                        BigInteger.valueOf(100),
                        BigInteger.valueOf(200),
                        destinationAccount,
                        BigInteger.valueOf(300),
                        BigInteger.valueOf(400),
                        "tx one"
                )
        );

        TransferRequest req = new TransferRequest(
                sourceAccount.getId(),
                destinationAccount.getId(),
                "EUR",
                "50"
        );

        MvcResult result = mockMvc.perform(
                        post("/transfer")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void transferWithConversionHappyPath() throws Exception {
        Client client = new Client(UUID.randomUUID());

        Account sourceAccount = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "USD",
                BigInteger.valueOf(1L)
        );

        Account destinationAccount = new Account(
                UUID.fromString("1ea6d532-faca-4240-aacf-0356b1e0e950"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(sourceAccount.getId())).thenReturn(
                Optional.of(sourceAccount)
        );

        when(accountsRepository.findById(destinationAccount.getId())).thenReturn(
                Optional.of(destinationAccount)
        );

        when(fxConverterService.convert("EUR", "USD", BigInteger.valueOf(50))).thenReturn(
                new FXConversionResult("EUR", "USD", BigInteger.valueOf(75))
        );

        Instant timestamp = Instant.ofEpochSecond(1715933225L);

        when(transferService.transfer(
                any(),
                eq(sourceAccount.getId()),
                eq(BigInteger.valueOf(75)),
                eq(destinationAccount.getId()),
                eq(BigInteger.valueOf(50)),
                anyString()
        )).thenReturn(
                new Transaction(
                        UUID.fromString("424dfead-4625-4dd4-a025-642542eebfdf"),
                        TransactionStatus.COMPLETED,
                        timestamp,
                        sourceAccount,
                        BigInteger.valueOf(100),
                        BigInteger.valueOf(200),
                        destinationAccount,
                        BigInteger.valueOf(300),
                        BigInteger.valueOf(400),
                        "tx one"
                )
        );

        TransferRequest req = new TransferRequest(
                sourceAccount.getId(),
                destinationAccount.getId(),
                "EUR",
                "50"
        );

        MvcResult result = mockMvc.perform(
                        post("/transfer")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void transferMissingAccount() throws Exception {
        Client client = new Client(UUID.randomUUID());

        Account sourceAccount = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "USD",
                BigInteger.valueOf(1L)
        );

        Account destinationAccount = new Account(
                UUID.fromString("1ea6d532-faca-4240-aacf-0356b1e0e950"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(sourceAccount.getId())).thenReturn(
                Optional.empty()
        );

        TransferRequest req = new TransferRequest(
                sourceAccount.getId(),
                destinationAccount.getId(),
                "EUR",
                "50"
        );

        MvcResult result = mockMvc.perform(
                        post("/transfer")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void transferMissingDestinationAccount() throws Exception {
        Client client = new Client(UUID.randomUUID());

        Account sourceAccount = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "USD",
                BigInteger.valueOf(1L)
        );

        Account destinationAccount = new Account(
                UUID.fromString("1ea6d532-faca-4240-aacf-0356b1e0e950"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(sourceAccount.getId())).thenReturn(
                Optional.of(sourceAccount)
        );

        when(accountsRepository.findById(destinationAccount.getId())).thenReturn(
                Optional.empty()
        );

        TransferRequest req = new TransferRequest(
                sourceAccount.getId(),
                destinationAccount.getId(),
                "EUR",
                "50"
        );

        MvcResult result = mockMvc.perform(
                        post("/transfer")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void transferSameAccount() throws Exception {
        Client client = new Client(UUID.randomUUID());

        Account sourceAccount = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "USD",
                BigInteger.valueOf(1L)
        );

        Account destinationAccount = new Account(
                UUID.fromString("1ea6d532-faca-4240-aacf-0356b1e0e950"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(sourceAccount.getId())).thenReturn(
                Optional.of(sourceAccount)
        );

        when(accountsRepository.findById(destinationAccount.getId())).thenReturn(
                Optional.of(sourceAccount)
        );

        TransferRequest req = new TransferRequest(
                sourceAccount.getId(),
                destinationAccount.getId(),
                "EUR",
                "50"
        );

        MvcResult result = mockMvc.perform(
                        post("/transfer")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void transferInvalidCurrency() throws Exception {
        Client client = new Client(UUID.randomUUID());

        Account sourceAccount = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "USD",
                BigInteger.valueOf(1L)
        );

        Account destinationAccount = new Account(
                UUID.fromString("1ea6d532-faca-4240-aacf-0356b1e0e950"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(sourceAccount.getId())).thenReturn(
                Optional.of(sourceAccount)
        );

        when(accountsRepository.findById(destinationAccount.getId())).thenReturn(
                Optional.of(destinationAccount)
        );

        TransferRequest req = new TransferRequest(
                sourceAccount.getId(),
                destinationAccount.getId(),
                "USD",
                "50"
        );

        MvcResult result = mockMvc.perform(
                        post("/transfer")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void transferInvalidAmount() throws Exception {
        Client client = new Client(UUID.randomUUID());

        Account sourceAccount = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "USD",
                BigInteger.valueOf(1L)
        );

        Account destinationAccount = new Account(
                UUID.fromString("1ea6d532-faca-4240-aacf-0356b1e0e950"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(sourceAccount.getId())).thenReturn(
                Optional.of(sourceAccount)
        );

        when(accountsRepository.findById(destinationAccount.getId())).thenReturn(
                Optional.of(destinationAccount)
        );

        TransferRequest req = new TransferRequest(
                sourceAccount.getId(),
                destinationAccount.getId(),
                "EUR",
                "0"
        );

        MvcResult result = mockMvc.perform(
                        post("/transfer")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void transferMalformedAmount() throws Exception {
        Client client = new Client(UUID.randomUUID());

        Account sourceAccount = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "USD",
                BigInteger.valueOf(1L)
        );

        Account destinationAccount = new Account(
                UUID.fromString("1ea6d532-faca-4240-aacf-0356b1e0e950"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(sourceAccount.getId())).thenReturn(
                Optional.of(sourceAccount)
        );

        when(accountsRepository.findById(destinationAccount.getId())).thenReturn(
                Optional.of(destinationAccount)
        );

        TransferRequest req = new TransferRequest(
                sourceAccount.getId(),
                destinationAccount.getId(),
                "EUR",
                "I'm a teapot!"
        );

        MvcResult result = mockMvc.perform(
                        post("/transfer")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is4xxClientError())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void transferWithConversionInvalidAmount() throws Exception {
        Client client = new Client(UUID.randomUUID());

        Account sourceAccount = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "USD",
                BigInteger.valueOf(1L)
        );

        Account destinationAccount = new Account(
                UUID.fromString("1ea6d532-faca-4240-aacf-0356b1e0e950"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(sourceAccount.getId())).thenReturn(
                Optional.of(sourceAccount)
        );

        when(accountsRepository.findById(destinationAccount.getId())).thenReturn(
                Optional.of(destinationAccount)
        );

        when(fxConverterService.convert("EUR", "USD", BigInteger.valueOf(50))).thenReturn(
                new FXConversionResult("EUR", "USD", BigInteger.valueOf(0))
        );

        Instant timestamp = Instant.ofEpochSecond(1715933225L);

        when(transferService.transfer(
                any(),
                eq(sourceAccount.getId()),
                eq(BigInteger.valueOf(75)),
                eq(destinationAccount.getId()),
                eq(BigInteger.valueOf(50)),
                anyString()
        )).thenReturn(
                new Transaction(
                        UUID.fromString("424dfead-4625-4dd4-a025-642542eebfdf"),
                        TransactionStatus.COMPLETED,
                        timestamp,
                        sourceAccount,
                        BigInteger.valueOf(100),
                        BigInteger.valueOf(200),
                        destinationAccount,
                        BigInteger.valueOf(300),
                        BigInteger.valueOf(400),
                        "tx one"
                )
        );

        TransferRequest req = new TransferRequest(
                sourceAccount.getId(),
                destinationAccount.getId(),
                "EUR",
                "50"
        );

        MvcResult result = mockMvc.perform(
                        post("/transfer")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is5xxServerError())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }

    @Test
    void transferThrowsException() throws Exception {
        Client client = new Client(UUID.randomUUID());

        Account sourceAccount = new Account(
                UUID.fromString("306b74d2-adea-4592-9f8a-981e80a7e041"),
                client,
                "USD",
                BigInteger.valueOf(1L)
        );

        Account destinationAccount = new Account(
                UUID.fromString("1ea6d532-faca-4240-aacf-0356b1e0e950"),
                client,
                "EUR",
                BigInteger.valueOf(1L)
        );

        when(accountsRepository.findById(sourceAccount.getId())).thenReturn(
                Optional.of(sourceAccount)
        );

        when(accountsRepository.findById(destinationAccount.getId())).thenReturn(
                Optional.of(destinationAccount)
        );

        when(fxConverterService.convert("EUR", "USD", BigInteger.valueOf(50))).thenReturn(
                new FXConversionResult("EUR", "USD", BigInteger.valueOf(75))
        );

        Instant timestamp = Instant.ofEpochSecond(1715933225L);

        when(transferService.transfer(
                any(),
                eq(sourceAccount.getId()),
                eq(BigInteger.valueOf(75)),
                eq(destinationAccount.getId()),
                eq(BigInteger.valueOf(50)),
                anyString()
        )).thenThrow(
                new TransferService.TransferException()
        );

        TransferRequest req = new TransferRequest(
                sourceAccount.getId(),
                destinationAccount.getId(),
                "EUR",
                "50"
        );

        MvcResult result = mockMvc.perform(
                        post("/transfer")
                                .content(asJsonString(req))
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is5xxServerError())
                .andReturn();

        expect(result.getResponse().getContentAsString()).toMatchSnapshot();
    }


}