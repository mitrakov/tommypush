package com.mitrakoff.self.tommypush;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mitrakoff.self.tommypush.comparer.Comparer;
import io.burt.jmespath.jackson.JacksonRuntime;
import okhttp3.*;
import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.*;
import java.util.*;

@SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "SameParameterValue", "InfiniteLoopStatement", "BusyWait"})
@Deprecated
public class Checker extends Thread {
    static public long PERIODIC_MSEC = 20000L;
    static public int COOLDOWN_MINUTES = 300;     // don't send duplicate messages during this time
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
        while (true) {
            final var minutes = Duration.between(lastSentMsgTime, LocalDateTime.now()).toMinutes();
            if (minutes >= COOLDOWN_MINUTES) {
                final var result = makeRequest();
                if (!result.isEmpty()) {
                    parseDouble(result, jmesPath).ifPresent(realRate -> {
                        if (comparer.compare(realRate, desiredRate)) {
                            recipientFcmTokens.forEach( token -> {
                                try {
                                    firebase.sendMessage(token, String.format("Эдуард Суровый: %s", name), String.format("%s %s %s!", realRate, comparer, desiredRate));
                                } catch (Throwable e) {e.printStackTrace();}
                            });
                            lastSentMsgTime = LocalDateTime.now();
                        }
                    });
                } else System.err.println("Error getting response");
            }
            try {Thread.sleep(PERIODIC_MSEC);} catch (InterruptedException e) {e.printStackTrace();}
        }
    }

    private String makeRequest() {
        final var body = jsonBody.map(s -> RequestBody.create(s, MediaType.parse(APPLICATION_JSON))).orElse(null);
        final var call = client.newCall(new Request.Builder().url(uri).method(method, body).build());
        try (final var response = call.execute()) {
            if (response.code() == 200)
                return response.body() != null ? response.body().string() : "";
            else { System.err.println(response.body()); return ""; }
        } catch (IOException e) {e.printStackTrace(); return "";}
    }

    private Optional<Double> parseDouble(String json, String jmesPath) {
        try {
            final var jq = jqRuntime.compile(jmesPath);
            final var node = mapper.readTree(json);
            final var result = jq.search(node);
            System.out.printf("%s: %s result is %s\n", LocalDateTime.now(), name, result);

            // Note that "Optional.ofNullable(result)" won't work because "result==null" may be false, but "result.isNull()" may be true
            return result.isNull() ? Optional.empty() : Optional.of(result.asDouble());
        } catch (IOException e) {e.printStackTrace(); return Optional.empty();}
    }

    private OkHttpClient buildClient(boolean secure) {
        if (secure) return new OkHttpClient();

        // insecure http client
        final var manager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {}
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {}
            @Override
            public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
        };

        try {
            final var sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{manager}, new SecureRandom());
            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), manager)
                    .hostnameVerifier((h,s) -> true)
                    .build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {throw new RuntimeException(e);}
    }
}
