package main.server;

import main.server.ITUtils.Client;
import org.awaitility.Duration;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private List<Client> clients = new ArrayList<>();

    private String hostname = "localhost";
    private int serverPort = 800;
    private static final Duration ACCEPTABLE_DELAY = Duration.TWO_HUNDRED_MILLISECONDS;

    @BeforeAll
    static void setUpServer(){
        Run serverRunner = new Run();
        String[] emptyArgs = {};

        Run.main(emptyArgs);
    }

    @AfterEach
    void disconnectClients() {
        disconnect(clients);
        clients.clear();
    }

    void setUpClients(int numberOfClients) {
        for (int i = 0; i < numberOfClients; i++) {
            Client client = new Client();
            client.setEnc(ISO_8859_1);

            clients.add(client);
        }
    }

    void connectClients() {
        clients.forEach(client -> client.connect(hostname, serverPort));
    }

    @Test
    void givenClient_whenConnectedAndSent_thenReceivedExpected() {
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
    void givenTwoClients_whenConnectedAndSent_thenReceivedExpected() {
        //Given
        setUpClients(2);
        String[] expectedMessages = {"message from client 0", "message from client 1"};
        final Map<Client, List<String>> clientMessagesMap = new HashMap<>();

        //When
        connectClients();
        sendMessage(clients.get(0), expectedMessages[0]);
        sendMessage(clients.get(1), expectedMessages[1]);

        //Then
        for (int i = 0; i < clients.size(); i++) {
            clientMessagesMap.put(clients.get(i), new ArrayList<>());

            var expectedMessagesPerClient = 2;
            int j = i;
            await().pollDelay(ACCEPTABLE_DELAY.divide(2))
                   .atMost(ACCEPTABLE_DELAY)
                   .until(() -> expectedMessagesPerClient == readMessages(clients.get(j), clientMessagesMap));
        };

        assertThat(extractTextMessages(clients.get(0), clientMessagesMap), containsInAnyOrder(expectedMessages));
        assertThat(extractTextMessages(clients.get(1), clientMessagesMap), containsInAnyOrder(expectedMessages));
    }

    @Test
    void givenClient_whenKeyRegistered_thenPublicServerKeyExpected() {
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
        for (int i = 0; i < clients.size(); i++) {
            clientMessagesMap.put(clients.get(i), new ArrayList<>());

            var expectedMessagesPerClient = 1;
            int j = i;
            await().pollDelay(ACCEPTABLE_DELAY.divide(2))
                   .atMost(ACCEPTABLE_DELAY)
                   .until(() -> expectedMessagesPerClient == readMessages(clients.get(j), clientMessagesMap));
        }

        assertThat(clientMessagesMap.get(clients.get(0)).get(0), containsString(expectedServerKeySignature));
    }

    private static void sendMessage(Client client, String message) {
        client.write(message);
    }

    private static int readMessages(Client client, Map<Client, List<String>> clientMessages) {
        int receivedMessageCount = 0;

        String receivedMessage = null;
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
            //This timeout is needed in order to let server invalidate connected users and disconnect them.
            //TODO: implement user disconnect on server side
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}