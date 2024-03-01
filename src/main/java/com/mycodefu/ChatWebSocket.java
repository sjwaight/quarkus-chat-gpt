package com.mycodefu;

import com.mycodefu.chat.C3P0;
import io.quarkiverse.langchain4j.ChatMemoryRemover;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.eclipse.microprofile.context.ManagedExecutor;

import java.io.IOException;

import static java.lang.StringTemplate.STR;

@ServerEndpoint("/chat/{name}/")
@ApplicationScoped
public class ChatWebSocket {
    private final C3P0 c3p0;
    private final ManagedExecutor managedExecutor;

    @Inject
    public ChatWebSocket(C3P0 c3p0, ManagedExecutor managedExecutor) {
        this.c3p0 = c3p0;
        this.managedExecutor = managedExecutor;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("name") String name) {
        System.out.println(STR."onOpen> name: \{name}");
        managedExecutor.execute(() -> {
            try {
                String response = c3p0.greet(session, name);
                System.out.println(STR."Response> \{response}");
                session.getBasicRemote().sendText(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @OnClose
    public void onClose(Session session, @PathParam("name") String name) {
        System.out.println(STR."onClose> name: \{name}");
        ChatMemoryRemover.remove(c3p0, session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("onError> " + ": " + throwable);
    }

    @OnMessage
    public void onMessage(Session session, @PathParam("name") String name, String message) {
        System.out.println(STR."onMessage> name: \{name}, message: \{message}");
        managedExecutor.execute(() -> {
            try {
                session.getBasicRemote().sendText(message);
                String response = c3p0.interact(session, message);
                session.getBasicRemote().sendText(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
