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
public class StartWebSocket {

    @Inject
    C3P0 c3p0;

    @Inject
    ManagedExecutor managedExecutor;

    @OnOpen
    public void onOpen(Session session, @PathParam("name") String name) {
        System.out.println(STR."onOpen> \{name}");
        managedExecutor.execute(() -> {
            try {
                String response = c3p0.greet(session, name);
                session.getBasicRemote().sendText(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @OnClose
    public void onClose(Session session, @PathParam("name") String name) {
        System.out.println("onClose> " + name);
        ChatMemoryRemover.remove(c3p0, session);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("onError> " + ": " + throwable);
    }

    @OnMessage
    public void onMessage(Session session, String message) throws IOException {
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
