package com.zigurs.ledger.api;

import com.zigurs.ledger.api.requests.AccountsRequest;
import com.zigurs.ledger.data.AccountsRepository;
import com.zigurs.ledger.data.ClientsRepository;
import com.zigurs.ledger.model.Account;
import com.zigurs.ledger.model.Client;

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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.jsonSnapshot.SnapshotMatcher.*;
import static org.hamcrest.core.StringContains.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static testutils.Utils.asJsonString;

@WebMvcTest(AccountsController.class)
class AccountsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientsRepository clientsRepository;

    @MockBean
    private AccountsRepository accountsRepository;

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

        when(clientsRepository.findById(client.getId())).thenReturn(
                Optional.of(client)
        );

        when(accountsRepository.findByClientId(client.getId())).thenReturn(
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
    void getAccountsNoAccounts() throws Exception {
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
    void getAccountsErrorWhenFetchingClient() throws Exception {
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
    void getAccountsErrorWhenFetchingAccounts() throws Exception {
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
}