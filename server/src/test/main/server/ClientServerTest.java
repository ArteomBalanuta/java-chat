package main.server;

import main.server.ITUtils.Client;
import main.server.engine.console.facade.GUIFacade;
import main.server.engine.console.facade.impl.GUIFacadeImpl;
import main.server.engine.console.service.CMDService;
import main.server.engine.console.service.GUIMessageService;
import main.server.engine.console.service.GUIService;
import main.server.engine.console.service.impl.CMDServiceImpl;
import main.server.engine.console.service.impl.GUIMessageServiceImpl;
import main.server.engine.console.service.impl.GUIServiceImpl;
import main.server.engine.server.facade.ChatEngine;
import main.server.engine.server.facade.impl.ChatEngineImpl;
import org.awaitility.Duration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@ExtendWith({MockitoExtension.class})
@TestInstance(PER_CLASS)
class ClientServerTest {
    private final String hostname = "localhost";
    private final int serverPort = 800;
    private static final Duration ACCEPTABLE_DELAY = Duration.TWO_HUNDRED_MILLISECONDS;

    private final List<Client> clients = new ArrayList<>();

    @BeforeAll
    static void setUpServer() {
        String[] args = new String[0];
        ApplicationRunner.main(args);
    }

    @AfterEach
    void disconnectClients() {
        disconnect(clients);
        clients.clear();
    }

    @Test
    void givenClient_whenConnectedAndSentMessage_thenReceivedExpectedMessage() {
        //Given
        setUpClients(1);
        Client client = clients.stream().findFirst().get();
        String message = "hello world";

        //When
        client.connect(hostname, serverPort);
        client.write(message);

        //Then
        var echoMsg = new AtomicReference<>();
        await().pollDelay(ACCEPTABLE_DELAY.divide(2))
               .atMost(ACCEPTABLE_DELAY)
               .until(() -> {
                    echoMsg.set(client.read());
                    return echoMsg.get() != null;
                });

        String actualMessage = echoMsg.toString().split(":")[1].trim();
        assertEquals(actualMessage, message);
    }

    @Test
    void givenServerAndTwoClients_whenConnectedAndSentMessages_thenReceivedExpectedMessages() {
        //Given
        setUpClients(2);
        String[] expectedMessages = {"message from client 0", "message from client 1"};
        final Map<Client, List<String>> clientMessagesMap = new HashMap<>();
        int expectedMessageCount = 2;

        //When
        connectClients();
        sendMessage(clients.get(0), expectedMessages[0]);
        sendMessage(clients.get(1), expectedMessages[1]);

        //Then
        readClientsMessages(clientMessagesMap, expectedMessageCount);

        assertThat(extractTextMessages(clients.get(0), clientMessagesMap), containsInAnyOrder(expectedMessages));
        assertThat(extractTextMessages(clients.get(1), clientMessagesMap), containsInAnyOrder(expectedMessages));
    }

    @Test
    void givenClientAndPublicKeyRegistrationMessage_whenMessageSend_thenReceivedExpectedPublicServerKey() {
        //Given
        setUpClients(1);
        String publicKeyMessage = "publicKey MIIBIzCBmQYJKoZIhvcNAQMBMIGLAoGBAP//////////yQ/aoiFowjT" +
                "ExmKLgNwc0SkCTgiKZ8x0Agu+pjsTmyJRSgh5jjQE3e+" +
                "VGbPNOkMbMCsKbfJfFDdP4TVtbVHCReSFtXZiXn7G9ExC6aY37WsL/1y29Aa37e44a" +
                "/taiZ+lrp8kEXxLH+ZJKGZR7OZTgf//////////AgECAgICAAOBhAACgYBdvqxf0BFz" +
                "bixJrdMTaIdcBe7BEjtpgga6lkqx0kFPJrm2FzkDTCVAF9C7e9Y6qDWVXS8itZemNhp" +
                "urYfogVgGZD40ARZYCaxVYRXBkXmRXs9Ijp5s0kyKYoTiIY6N+UFu8CxL6YY6YSPduC" +
                "PeVGbk1G/VF8/4fu1Xo874DSqHyA==";

        String expectedServerKeySignature = "serverPublicKey";
        final Map<Client, List<String>> clientMessagesMap = new HashMap<>();

        //When
        connectClients();
        sendMessage(clients.get(0), publicKeyMessage);

        //Then
        readClientsMessages(clientMessagesMap, 1);

        assertThat(clientMessagesMap.get(clients.get(0)).get(0), containsString(expectedServerKeySignature));
    }

    private void setUpClients(int numberOfClients) {
        for (int i = 0; i < numberOfClients; i++) {
            Client client = new Client();
            client.setEnc(ISO_8859_1);

            clients.add(client);
        }
    }

    private void connectClients() {
        clients.forEach(client -> client.connect(hostname, serverPort));
    }

    private static void sendMessage(Client client, String message) {
        client.write(message);
    }

    private void readClientsMessages(Map<Client, List<String>> clientMessagesMap, int expectedMessagesPerClient) {
        for (int i = 0; i < clients.size(); i++) {
            clientMessagesMap.put(clients.get(i), new ArrayList<>());
            int j = i;

            await().pollDelay(ACCEPTABLE_DELAY.divide(2))
                    .atMost(ACCEPTABLE_DELAY)
                    .until(() -> expectedMessagesPerClient == readMessages(clients.get(j), clientMessagesMap));
        }
    }

    private static int readMessages(Client client, Map<Client, List<String>> clientMessages) {
        int receivedMessageCount = 0;

        String receivedMessage;
        while ((receivedMessage = client.read()) != null) {
            clientMessages.get(client).add(receivedMessage);
            receivedMessageCount += 1;
        }

        return receivedMessageCount;
    }

    private static List<String> extractTextMessages(Client client, Map<Client, List<String>> clientMessagesMap) {
        int textBlockOffset = 1;
        return clientMessagesMap.get(client).stream()
                .map(message -> message.split(":")[textBlockOffset].trim())
                .collect(Collectors.toList());
    }

    private static void disconnect(List<Client> clients) {
        clients.forEach(Client::disconnect);
        try {
            //This timeout is needed in order to let server invalidate connected users and disconnect them in time.
            //TODO: implement user disconnect on server side
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}