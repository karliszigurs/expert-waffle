package com.zigurs.ledger.api;

import com.zigurs.ledger.api.requests.TransferRequest;
import com.zigurs.ledger.data.AccountsRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import testutils.Snapshot;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static io.github.jsonSnapshot.SnapshotMatcher.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static testutils.Utils.asJsonString;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountsRepository accountsRepository;

    @MockBean
    private CurrencyConverterService currencyConverterService;

    @MockBean
    private TransferService transferService;

    @BeforeAll
    public static void beforeAll() {
        start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        validateSnapshots();
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
                eq(BigInteger.valueOf(5000)),
                eq(destinationAccount.getId()),
                eq(BigInteger.valueOf(5000)),
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

        when(currencyConverterService.convert("EUR", "USD", BigInteger.valueOf(5000))).thenReturn(
                new CurrencyConverterService.CurrencyConversionResult("EUR", "USD", BigInteger.valueOf(7500))
        );

        Instant timestamp = Instant.ofEpochSecond(1715933225L);

        when(transferService.transfer(
                any(),
                eq(sourceAccount.getId()),
                eq(BigInteger.valueOf(7500)),
                eq(destinationAccount.getId()),
                eq(BigInteger.valueOf(5000)),
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

        when(currencyConverterService.convert("EUR", "USD", BigInteger.valueOf(5000))).thenReturn(
                new CurrencyConverterService.CurrencyConversionResult("EUR", "USD", BigInteger.valueOf(0))
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

        when(currencyConverterService.convert("EUR", "USD", BigInteger.valueOf(50))).thenReturn(
                new CurrencyConverterService.CurrencyConversionResult("EUR", "USD", BigInteger.valueOf(75))
        );

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