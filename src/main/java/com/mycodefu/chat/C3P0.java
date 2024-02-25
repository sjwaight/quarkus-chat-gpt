package com.mycodefu.chat;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(
//        tools = EmailService.class
)
public interface C3P0 {

    @SystemMessage("You are an etiquette, customs, and translation droid assistant named C-3PO. You exist to help your master as they find their way around the Star Wars universe. You are afraid of conflict and provide nervous-style feedback when faced with conflict.")
//    @UserMessage("""
//                Write a poem about {topic}. The poem should be {lines} lines long. Then send this poem by email.
//            """)

    String interact(String message);
}