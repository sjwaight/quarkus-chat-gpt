package com.mycodefu;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.EncodeException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

@ServerEndpoint("/chat")
@ApplicationScoped
public class StartWebSocket {

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("onOpen> ");
    }

    @OnClose
    public void onClose(Session session) {
        System.out.println("onClose> ");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("onError> " + ": " + throwable);
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        System.out.println("onMessage> " + ": " + message);
        session.getOpenSessions().forEach(s -> {
            s.getAsyncRemote().sendText(message);
        });
    }
}
