package com.mycodefu.chat;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.regex;
import static java.util.regex.Pattern.compile;

@ApplicationScoped
public class StarWarsDB {

    @Inject
    MongoClient mongoClient;

    @Tool("Get details about a person from the star wars world.")
    public String getCharacterInfo(String name) {
        Bson query = regex("name", compile(STR.".*\{name}.*", Pattern.CASE_INSENSITIVE));
        Log.info(STR."Querying MongoDB StarWars people: \{query}");

        MongoCollection<Document> collection = mongoClient.getDatabase("StarWars").getCollection("people");
        Document person = collection.find(query).first();
        String personData = person != null ? person.toJson() : null;

        Log.info(STR."Found person: \{personData}");
        return personData;
    }

}
