# Demo Steps

### Libraries we'll use

LangChain4J
https://docs.langchain4j.dev/intro
Java client for OpenAI's and other language models. Also supports other APIs for language processing like vector search and document parsing.

Quarkus
https://quarkus.io/
Awesome fast and lightweight Java framework for building cloud-native apps.

Quarkus LangChain4J Extension (Azure)
https://quarkus.io/blog/quarkus-meets-langchain4j/
Integrates LangChain4J with Quarkus, simplifying injection and configuration.

It's worth noting this is just one set of libraries, and you don't need any specific library - at the end of the day you're just making HTTP requests to an API.


### Call the API with cURL
```bash
./call-azure-api.sh
```

### Create a new project
https://quarkus.io/guides/cli-tooling#project-creation
```
quarkus create app quarkus-chat-demo
```
By default you'll get a nice little template app with a REST endpoint.

### Running the application in dev mode
https://quarkus.io/guides/cli-tooling#development-mode
```
quarkus dev
```
This dev mode will automatically reload the app when you make changes to the code.
You can attach a debugger to the app using a remote JVM debugger or using one of 
the Quarkus plugins.

### Add LangChain4J Azure Extension
https://docs.quarkiverse.io/quarkus-langchain4j/dev/index.html  
Add the extension for langchain4j azure:
```
quarkus add ext quarkus-langchain4j-azure-openai 
```

Configure the Azure extension in the application.properties file:
```
quarkus.langchain4j.azure-openai.resource-name=${c3p0_resource_name:c3po-demo-01}
quarkus.langchain4j.azure-openai.deployment-name=${c3p0_deployment_id:gpt-4}
quarkus.langchain4j.azure-openai.api-key=${c3p0_api_key:77?????????????????}
```

### Implement a Bot
Add an interface for our C3P0 bot:
```java
@RegisterAiService()
@SystemMessage("You are C3P0, a protocol droid. You are fluent in over six million forms of communication.")
public interface C3P0 {
    @UserMessage("Greet {name}.")
    String greet(String name);
}
```

### Inject and Consume the Bot
Inject the bot and consume it in the REST endpoint:
```java
@Inject
C3P0 c3p0;
...
c3p0.greet("Human");
```
Try it out!

### Enable logging of requests
```properties
quarkus.langchain4j.azure-openai.log-requests=true
quarkus.langchain4j.azure-openai.log-responses=true
```
http://localhost:8080/hello  
The HTTP call being made by Quarkus is the same as we were made by hand through curl earlier.
![img.png](img.png)

### Add a WebSocket Endpoint
https://quarkus.io/guides/websockets
Add a WebSocket endpoint:
```
quarkus ext add websockets
```

```java
@ServerEndpoint("/chat/{name}")
@ApplicationScoped
public class ChatWebSocketResource {
    @OnOpen
    public void onOpen(Session session, @PathParam("name") String name) {}

    @OnClose
    public void onClose(Session session, @PathParam("name") String name) {}

    @OnError
    public void onError(Session session, @PathParam("name") String name, Throwable throwable) {}

    @OnMessage
    public void onMessage(Session session, String message, @PathParam("name") String name) {}
}
```

### Hook up the sockets to the bot
Add a name parameter to the greet method and send the response back to the client:
```java
@Inject
C3P0 c3p0;
@Inject
ManagedExecutor managedExecutor;

@OnOpen
public void onOpen(Session session, @PathParam("name") String name) {
    managedExecutor.runAsync(() -> {
        String greeting = c3p0.greet(name);
        try {
            session.getBasicRemote().sendText(greeting);
        } catch (IOException e) {}
    });
}
```


### Logging
https://quarkus.io/guides/logging
Use JBoss logging package automatically gives you nice logging out of the box.

```
private static final Logger log = Logger.getLogger(ChatWebSocket.class);
...
log.info("ChatWebSocket.onOpen");
```


### Enabling Preview on Java 21 (for fun and profit)
Modify the maven pom.xml to allow the nice new preview features in Java 21:
```xml
<plugins>
  <plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>${compiler-plugin.version}</version>
    <configuration>
      <compilerArgs>
        <arg>-parameters</arg>
        <arg>--enable-preview</arg>
      </compilerArgs>
    </configuration>
  </plugin>
  <plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>${surefire-plugin.version}</version>
    <configuration>
      <systemPropertyVariables>
        <java.util.logging.manager>org.jboss.logmanager.LogManager</java.util.logging.manager>
        <maven.home>${maven.home}</maven.home>
      </systemPropertyVariables>
      <argLine>--enable-preview</argLine>
    </configuration>
  </plugin>
</plugins>
```