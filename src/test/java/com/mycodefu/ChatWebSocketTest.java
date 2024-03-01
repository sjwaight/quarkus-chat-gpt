package com.mycodefu;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.quarkiverse.wiremock.devservice.ConnectWireMock;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.websocket.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@QuarkusTest
@ConnectWireMock
class ChatWebSocketTest {
    private static final LinkedBlockingDeque<String> MESSAGES = new LinkedBlockingDeque<>();

    @TestHTTPResource("/chat/Leia/")
    URI uri;

    WireMock wireMock;

    @Test
    void testMessaging_Greeting() throws DeploymentException, IOException, InterruptedException {
        wireMockResponse("Hello Leia! I am C3P0, human-cyborg relations. I am fluent in over six million forms of communication.");
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
            Assertions.assertEquals("Hello Leia! I am C3P0, human-cyborg relations. I am fluent in over six million forms of communication.", MESSAGES.poll(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void testMessaging_Conversation() throws DeploymentException, IOException, InterruptedException {
        wireMockResponse("Hello Leia! I am C3P0, human-cyborg relations. I am fluent in over six million forms of communication.");

        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));

            //C3p0 Should Greet on connection
            Assertions.assertEquals("Hello Leia! I am C3P0, human-cyborg relations. I am fluent in over six million forms of communication.", MESSAGES.poll(10, TimeUnit.SECONDS));

            wireMockResponse("I don't want to know about them! Hand them to Darth Vader!");

            session.getBasicRemote().sendText("Hello! I have the plans for the death star!");

            //Any sent message should be echo'd back
            Assertions.assertEquals("Hello! I have the plans for the death star!", MESSAGES.poll(10, TimeUnit.SECONDS));

            //C3p0 should respond to the message
            Assertions.assertEquals("I don't want to know about them! Hand them to Darth Vader!", MESSAGES.poll(10, TimeUnit.SECONDS));
        }
    }


    @Test
    void testMessaging_GreetingError() throws DeploymentException, IOException, InterruptedException {
        wireMockResponse(500, "BAD REQUEST");
        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
            Assertions.assertEquals("Hello Leia. Unfortunately, my systems are reporting an error.", MESSAGES.poll(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void testMessaging_MessageError() throws DeploymentException, IOException, InterruptedException {
        wireMockResponse("Hello Leia! I am C3P0, human-cyborg relations. I am fluent in over six million forms of communication.");

        try (Session session = ContainerProvider.getWebSocketContainer().connectToServer(Client.class, uri)) {
            Assertions.assertEquals("CONNECT", MESSAGES.poll(10, TimeUnit.SECONDS));
            Assertions.assertEquals("Hello Leia! I am C3P0, human-cyborg relations. I am fluent in over six million forms of communication.", MESSAGES.poll(10, TimeUnit.SECONDS));

            wireMockResponse(500, "BAD REQUEST");

            session.getBasicRemote().sendText("Hello!");
            Assertions.assertEquals("Hello!", MESSAGES.poll(10, TimeUnit.SECONDS));

            //C3p0 should not respond to the message
            Assertions.assertEquals("Unfortunately, my systems are reporting an error.", MESSAGES.poll(10, TimeUnit.SECONDS));
        }
    }

    private void wireMockResponse(String response) {
        wireMockResponse(200, STR."""
                        {
                            "id":"chatcmpl-8xs4jWbWVv3iopXegymSQnnpwg6MH",
                            "object":"chat.completion",
                            "created":1709279909,
                            "model":"gpt-4",
                            "choices":[
                                {
                                    "finish_reason":"stop",
                                    "index":0,
                                    "message":{
                                        "role":"assistant",
                                        "content":"\{response}"
                                    }
                                }
                            ],
                            "usage":{
                                "prompt_tokens":77,
                                "completion_tokens":34,
                                "total_tokens":111
                            },
                            "system_fingerprint":"fp_68a7d165bf"
                            }""");
    }
    private void wireMockResponse(int status, String body) {
        wireMock.resetMappings();
        wireMock.register(
                post(urlEqualTo("/chat/completions?api-version=2023-05-15"))
                .willReturn(
                        aResponse()
                                .withStatus(status)
                                .withBody(body)
                )
        );
    }

    @ClientEndpoint
    public static class Client {

        @OnOpen
        public void open(Session session) {
            MESSAGES.add("CONNECT");
        }

        @OnMessage
        void message(String msg) {
            MESSAGES.add(msg);
        }
    }

    @AfterAll
    static void afterAll() {
        System.out.flush();
    }
}