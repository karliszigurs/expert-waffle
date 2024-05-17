package com.zigurs.mintos.ledger;

import com.zigurs.mintos.ledger.api.LedgerController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LedgerApplicationTests {

    @Autowired
    private LedgerController controller;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }

}
