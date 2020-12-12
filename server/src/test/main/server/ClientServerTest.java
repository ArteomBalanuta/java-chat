package main.server;

import main.server.ITUtils.Client;
import org.awaitility.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

@ExtendWith(MockitoExtension.class)
class ClientServerTest {
    private Run serverRunner;
    private String[] emptyArgs = {};
    private List<Client> clients = new ArrayList<>();

    private String hostname = "localhost";
    private int serverPort = 800;

    @BeforeEach
    void setUpAndStartServer() {
        serverRunner = new Run();
        Run.main(emptyArgs);
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
    void givenServerAndClient_whenConnectedAndSent_thenReceivedExpected() {
        //Given
        setUpClients(1);
        Client client = clients.stream().findFirst().get();
        String message = "hello world";

        //When
        client.connect(hostname, serverPort);
        client.write(message);

        //Then
        var echoMsg = new AtomicReference<>();
        await().atMost(Duration.TWO_HUNDRED_MILLISECONDS)
                .until(() -> {
                    echoMsg.set(client.read());
                    return echoMsg.get() != null;
                });

        String actualMessage = echoMsg.toString().split(":")[1].trim();
        assertEquals(actualMessage, message);
    }

    @Test
    void givenServerAndTwoClients_whenConnectedAndSent_thenReceivedExpected() {
        //Given
        setUpClients(2);
        String[] expectedMessages = {"message from client 0", "message from client 1"};
        final Map<Client, List<String>> clientMessagesMap = new HashMap<>();

        //When
        connectClients();
        for (int i = 0; i < clients.size(); i++) {
            String message = "message from client " + i;
            clients.get(i).write(message);
        }

        //Then
        for (int i = 0; i < clients.size(); i++) {
            clientMessagesMap.put(clients.get(i), new ArrayList<>());

            var expectedMessagesPerClient = 2;
            int j = i;
            await().atMost(Duration.TWO_HUNDRED_MILLISECONDS)
                    .until(() -> expectedMessagesPerClient == readMessages(clients.get(j), clientMessagesMap));
        };

        assertThat(extractTextMessages(clients.get(0), clientMessagesMap), containsInAnyOrder(expectedMessages));
        assertThat(extractTextMessages(clients.get(1), clientMessagesMap), containsInAnyOrder(expectedMessages));
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
}