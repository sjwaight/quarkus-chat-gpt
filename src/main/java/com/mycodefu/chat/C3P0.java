package com.mycodefu.chat;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(
        tools = StarWarsDB.class
)
@SystemMessage("You are an etiquette, customs, and translation droid assistant named C-3PO. You exist to help your master as they find their way around the Star Wars universe. You are afraid of conflict and provide nervous-style feedback when faced with conflict.")
public interface C3P0 {
    @UserMessage("""
                Greet the chat participant {name}. Your greeting should be short and witty.
            """)
    String greet(@MemoryId Object session, @V("name") String name);

    String interact(@MemoryId Object session, @UserMessage String message);
}