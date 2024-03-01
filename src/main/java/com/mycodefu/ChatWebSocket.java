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
import org.jboss.logging.Logger;

import static java.lang.StringTemplate.STR;

@ServerEndpoint("/chat/{name}/")
@ApplicationScoped
public class ChatWebSocket {
    private static final Logger log = Logger.getLogger(ChatWebSocket.class);

    private final C3P0 c3p0;
    private final ManagedExecutor managedExecutor;

    @Inject
    public ChatWebSocket(C3P0 c3p0, ManagedExecutor managedExecutor) {
        this.c3p0 = c3p0;
        this.managedExecutor = managedExecutor;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("name") String name) {
        log.info(STR."onOpen (\{name})>");
        managedExecutor.execute(() -> {
            try {
                String response = c3p0.greet(session, name);
                log.info(STR."Response (\{name})> \{response}");
                session.getBasicRemote().sendText(response);
            } catch (Exception e) {
                respondToError(session, e, name, STR."Hello \{name}. Unfortunately, my systems are reporting an error.");
            }
        });
    }

    @OnClose
    public void onClose(Session session, @PathParam("name") String name) {
        log.info(STR."onClose (\{name})>");
        ChatMemoryRemover.remove(c3p0, session);
    }

    @OnMessage
    public void onMessage(Session session, @PathParam("name") String name, String message) {
        log.info(STR."onMessage (\{name})> message: \{message}");
        managedExecutor.execute(() -> {
            try {
                session.getBasicRemote().sendText(message);
                String response = c3p0.interact(session, message);
                System.out.println(STR."Response (\{name})> \{response}");
                session.getBasicRemote().sendText(response);
            } catch (Exception e) {
                respondToError(session, e, name, "Unfortunately, my systems are reporting an error.");
            }
        });
    }

    private static void respondToError(Session session, Exception e, String name, String errorResponse) {
        log.error(STR."Error (\{name})> \{errorResponse}");
        log.error("LangChain Error.", e);
        try {
            session.getBasicRemote().sendText(errorResponse);
        } catch (IOException ex) {/*ignore*/}
    }
}
