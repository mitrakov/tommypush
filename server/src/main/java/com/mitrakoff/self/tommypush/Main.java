package com.mitrakoff.self.tommypush;

import com.mitrakoff.self.tommypush.comparer.*;
import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
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
        final String fcmToken2 = "d2-l9NbZSJ6s9jBkLXo_Ya:APA91bGat-LFN7OAMjTwgAKu8wm7_wzK7DmUU0__PnuDzIqgEP5909HUqHjItZNabVTyrYFtzkZKK1fMgd2KXnJIGGXkrTc9Ofeyhm3smQouCcPglq3u9TzRpw_wieA52Gv1MgTODLaJ";
        // Nelly
        final String fcmToken3 = "cOlSdL6IRHK1iPQK-gYq3-:APA91bFb3SQepLl0epysO219nmH9eNGHx2moNRqs5RnzIYP0eicCJNlmwSAR6U8A6Lvx7G6BzzRVQFKnA_RXXH-N5LWxWvFqvTzfAGAt88ed9B4XKeppEPenKoFsF322-HOsjlZMihpy";
        // iPhone X
        final String fcmToken4 = "e5BOi9E7Gk9JhyKDCP2bNy:APA91bHLj9FoQRZKfHH0UGMA8ChP1TuZ7R1abYpkTv65arx__oUsBZfFyKRA3yQ7VrEJCFDOGLb5NKvuICAAiNEXePzvRm-Ig1FkIxFjAuvZckPmiixlruZxo8p5KnQ9XLe2O2ZttlGQ";

        final FirebaseHelper firebase = new FirebaseHelper(firebaseConfPath);
        final String usdToRubPath  = "https://iss.moex.com/iss/engines/currency/markets/selt/securities.jsonp?iss.meta=off&iss.only=marketdata&securities=CETS:USD000UTSTOM";
        final String aviasalesPath = "https://ariadne.aviasales.ru/api/gql";    // GET: "https://lyssa.aviasales.ru/price_matrix?origin_iata=EVN&destination_iata=LED&depart_start=2023-01-06&depart_range=0"; jq="min(prices[].value)"

        final List<Checker> checkers = Arrays.asList(
            new Checker("USD", "GET", usdToRubPath, Optional.empty(), "marketdata.data[0][8]", new GreaterComparer(), desiredUsdRate, firebase, Collections.singletonList(fcmToken2)),
            new Checker("Aviasales", "POST", aviasalesPath, Optional.of(aviasalesJson()), "data.best_prices_v2.cheapest_direct.value", new LessComparer(), desiredAviasalesRate, firebase, Arrays.asList(fcmToken2))
        );

        checkers.forEach(Thread::start);
    }

    private static String aviasalesJson() {
        try (final InputStream is = Main.class.getClassLoader().getResourceAsStream("aviasales.json")) {
            if (is == null) throw new RuntimeException("Cannot read json");
            return new String(is.readAllBytes());
        } catch (IOException e) {throw new RuntimeException(e);}
    }
}
