package com.mitrakoff.self.tommypush;

import com.mitrakoff.self.tommypush.comparer.*;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage:   java -jar tommypush.jar <path/to/firebase.json> <usd-rate> <aviasales-rate>\nExample: java -jar tommypush.jar /home/user1/firebase-adminsdk.json 76.2 10000");
            System.exit(1);
        }

        final String firebaseConfPath = args[0];
        final double desiredUsdRate = Double.parseDouble(args[1]);
        final double desiredAviasalesRate = Double.parseDouble(args[2]);

        // iPhone7
        final String fcmToken1 = "cVXnVdTcM00lnKcKq4zrBn:APA91bFBMJQ87ryzNipCIVwlXOpwNl-3RWjOTw1Ei3yAFL6q3wr7bkVzRmXMeFYhtFYhEKlfogztMKTTRK4sVBLkLpCGh0NOCicbwqUjF2hJ-shta-lspkZBuTU5MWS6R2-cdBbmYvM1";
        // Samsung Galaxy S7
        final String fcmToken2 = "fKloxEYmR7CinffPjQgUn2:APA91bE0-dyq0RRj9k1SFol8kO6xX39QnhQh2sHzZNTxRpZ_0cLlxhymlwA9TEKuIKOOLjG2bjPHjEv7iOA7fgWtB1sn1JX8oCev6j3EMHpmKfUZXybECpCUtUmqTSzWrpP-Yz6Awn2j";
        // Nelly
        final String fcmToken3 = "cOlSdL6IRHK1iPQK-gYq3-:APA91bFb3SQepLl0epysO219nmH9eNGHx2moNRqs5RnzIYP0eicCJNlmwSAR6U8A6Lvx7G6BzzRVQFKnA_RXXH-N5LWxWvFqvTzfAGAt88ed9B4XKeppEPenKoFsF322-HOsjlZMihpy";

        final FirebaseHelper firebase = new FirebaseHelper(firebaseConfPath);
        final String usdToRubPath  = "https://iss.moex.com/iss/engines/currency/markets/selt/securities.jsonp?iss.meta=off&iss.only=marketdata&securities=CETS:USD000UTSTOM";
        final String aviasalesPath = "https://ariadne.aviasales.ru/api/gql";    // GET: "https://lyssa.aviasales.ru/price_matrix?origin_iata=EVN&destination_iata=LED&depart_start=2023-01-06&depart_range=0"; jq="min(prices[].value)"

        final List<Checker> checkers = Arrays.asList(
            new Checker("USD", "GET", usdToRubPath, Optional.empty(), "marketdata.data[0][8]", new GreaterComparer(), desiredUsdRate, firebase, Collections.singletonList(fcmToken2)),
            new Checker("Aviasales-Feb23", "POST", aviasalesPath, aviasalesJson(), "data.best_prices_v2.cheapest_direct.value", new LessComparer(), desiredAviasalesRate, firebase, Arrays.asList(fcmToken2))
        );

        checkers.forEach(Thread::start);
    }

    private static Optional<String> aviasalesJson() throws IOException {
        try (final InputStream is = Main.class.getClassLoader().getResourceAsStream("aviasales.json")) {
            return Optional.ofNullable(is).map(i -> {
                try {
                    return new String(i.readAllBytes());
                } catch (IOException e) {e.printStackTrace(); return null;}
            });
        }
    }
}
