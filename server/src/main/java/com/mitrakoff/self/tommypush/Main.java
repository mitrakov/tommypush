package com.mitrakoff.self.tommypush;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import java.io.File;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage:   java -jar tommypush.jar firebase.json app.json\nExample: java -jar tommypush.jar /home/user1/firebase-adminsdk.json /home/user1/application.json");
            System.exit(1);
        }

        final var firebaseConfPath = args[0];
        final var appConfPath = args[1];
        final var isDebug = args.length == 3;
        final var mapper = new ObjectMapper();
        final var client = new OkHttpClient();
        final var firebase = new FirebaseHelper(firebaseConfPath);

        while (true) {
            final var now = LocalDateTime.now();
            final var h = now.getHour();
            final var m = now.getMinute();
            if (isDebug || ((h == 12 || h == 18 || h == 0) && m == 0)) {
                final var config = mapper.readTree(new File(appConfPath));
                final var checkerList = config.get("checkers");
                final var fcmTokens = config.get("fcmTokens");
                if (checkerList == null || fcmTokens == null) throw new IllegalAccessException("Specify 'checkers' and 'fcmTokens' arrays in " + appConfPath);

                for (final JsonNode checkerNode : checkerList) {
                    final var checkerClass = checkerNode.get("class");
                    final var method = checkerNode.get("method");
                    final var uri = checkerNode.get("uri");
                    final var formatMessage = checkerNode.get("formatMessage");
                    final var jsonBody = checkerNode.get("body");
                    if (checkerClass == null || method == null || uri == null || formatMessage == null || jsonBody == null)
                        throw new IllegalAccessException("Specify 'class', 'method', 'uri', 'formatMessage' and 'body' for each checker");

                    final Checker2 checker = (Checker2) Class.forName(checkerClass.textValue()).getDeclaredConstructor(String.class).newInstance(formatMessage.asText());

                    try { // TODO: only POST implemented, add other http methods
                        final var body = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json"));
                        final var call = client.newCall(new Request.Builder().url(uri.asText()).method(method.asText(), body).build());
                        System.out.printf("%s: Making %s call to %s\n", LocalDateTime.now(), method, uri);
                        try (final var response = call.execute()) {
                            if (response.code() == 200) {
                                final var respBody = response.body() != null ? response.body().string() : "";
                                final var node = mapper.readTree(respBody);
                                final var result = checker.handleJson(node);
                                for (final JsonNode token : fcmTokens) {
                                    final var fcmToken = token.asText();
                                    firebase.sendMessage(fcmToken, checker.getClass().getSimpleName(), result);
                                }
                            } else { System.err.println(response.body()); }
                        }
                    } catch (Throwable e) {e.printStackTrace();}
                }
            }
            Thread.sleep(60 * 1000);
        }
    }
}
