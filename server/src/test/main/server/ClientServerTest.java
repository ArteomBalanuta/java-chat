package main.server;

import main.server.ITUtils.Client;
import org.awaitility.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ClientServerTest {
    private Run serverRunner;
    private Client client;

    @BeforeEach
    void setUpServer() {
        serverRunner = new Run();
        client = new Client();
        client.setEnc(ISO_8859_1);
    }

    @Test
    void givenServer_whenConnectedAndSent_thenReceivedExpected() {
        //Given
        String[] emptyArgs = {};
        String message = "hello world";

        //When
        Run.main(emptyArgs);
        client.setConnection("localhost", 800);
        client.write(message);

        //Then
        AtomicReference<String> echoMsg = new AtomicReference<>(client.read());
        await().atMost(Duration.TWO_HUNDRED_MILLISECONDS)
                .until(() -> {
                    echoMsg.set(client.read());
                    return echoMsg.get() != null;
                });

        String actualMessage = echoMsg.toString().split(":")[1].trim();
        assertAll(() -> assertNotNull(echoMsg),
                  () -> assertEquals(actualMessage, message));
    }
}