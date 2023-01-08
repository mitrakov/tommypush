package com.mitrakoff.self.tommypush;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitrakoff.self.tommypush.comparer.Comparer;
import io.burt.jmespath.Expression;
import io.burt.jmespath.jackson.JacksonRuntime;
import okhttp3.*;
import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.*;
import java.util.*;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SameParameterValue", "InfiniteLoopStatement", "BusyWait"})
public class Checker extends Thread {
    static public int COOLDOWN_MINUTES = 240;     // don't send duplicate messages during this time
    static public final String APPLICATION_JSON = "application/json";

    private final String name;
    private final String method;
    private final String uri;
    private final Optional<String> jsonBody;
    private final String jmesPath;
    private final Comparer comparer;
    private final Double desiredRate;
    private final FirebaseHelper firebase;
    private final List<String> recipientFcmTokens;

    public Checker(String name,
                   String method,
                   String uri,
                   Optional<String> jsonBody,
                   String jmesPath,
                   Comparer comparer,
                   Double desiredRate,
                   FirebaseHelper firebase,
                   List<String> recipientFcmTokens) {

        this.name = name;
        this.method = method;
        this.uri = uri;
        this.jsonBody = jsonBody;
        this.jmesPath = jmesPath;
        this.comparer = comparer;
        this.desiredRate = desiredRate;
        this.firebase = firebase;
        this.recipientFcmTokens = recipientFcmTokens;
    }

    private final OkHttpClient client = buildClient(false);
    public final JacksonRuntime jqRuntime = new JacksonRuntime();
    public final ObjectMapper mapper = new ObjectMapper();
    private LocalDateTime lastSentMsgTime = LocalDateTime.MIN;

    @Override
    public void run() {
        while (true) try {
            final long minutes = Duration.between(lastSentMsgTime, LocalDateTime.now()).toMinutes();
            if (minutes >= COOLDOWN_MINUTES) {
                final String result = makeRequest();
                if (!result.isEmpty()) {
                    final double realRate = parseDouble(result, jmesPath);
                    if (comparer.compare(realRate, desiredRate)) {
                        recipientFcmTokens.forEach( token -> {
                            try {
                                firebase.sendMessage(token, String.format("Эдуард Суровый: %s", name), String.format("%s %s %s!", realRate, comparer, desiredRate));
                            } catch (Throwable e) {e.printStackTrace();}
                        });
                        lastSentMsgTime = LocalDateTime.now();
                    }
                } else System.err.println("No response body");
            }
            Thread.sleep(10000L);
        } catch (Throwable e) {e.printStackTrace();}
    }

    private String makeRequest() throws IOException {
        final RequestBody body = jsonBody.map(s -> RequestBody.create(s, MediaType.parse(APPLICATION_JSON))).orElse(null);
        final Call call = client.newCall(new Request.Builder().url(uri).method(method, body).build());
        try (final Response response = call.execute()) {
            return response.body() != null ? response.body().string() : "";
        }
    }

    private double parseDouble(String json, String jmesPath) throws IOException {
        final Expression<JsonNode> jq = jqRuntime.compile(jmesPath);
        final JsonNode node = mapper.readTree(json);
        final JsonNode result = jq.search(node);
        System.out.printf("%s result is %s\n", name, result);

        return result.asDouble();
    }

    private OkHttpClient buildClient(boolean secure) {
        if (secure) return new OkHttpClient();

        // insecure http client
        final X509TrustManager manager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            @Override
            public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
        };

        try {
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{manager}, new SecureRandom());
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), manager)
                    .hostnameVerifier((h,s) -> true)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }
}
