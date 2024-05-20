package com.zigurs.ledger.api.requests;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferRequestTest {

    @Test
    void assertConstructor() {
        UUID sourceUUID = UUID.randomUUID();
        UUID destinationUUID = UUID.randomUUID();

        assertThrows(NullPointerException.class, () -> {
                    new TransferRequest(
                            null,
                            destinationUUID,
                            "XYZ",
                            "123"
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransferRequest(
                            sourceUUID,
                            null,
                            "XYZ",
                            "123"
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransferRequest(
                            sourceUUID,
                            destinationUUID,
                            null,
                            "123"
                    );
                }
        );

        assertThrows(NullPointerException.class, () -> {
                    new TransferRequest(
                            sourceUUID,
                            destinationUUID,
                            "XYZ",
                            null
                    );
                }
        );
    }
}