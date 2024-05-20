package com.zigurs.ledger;

import com.zigurs.ledger.api.TransferController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class LedgerApplicationTests {

    @Autowired
    private TransferController controller;

    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }
}
