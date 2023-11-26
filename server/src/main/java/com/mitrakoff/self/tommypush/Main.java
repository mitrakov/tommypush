package com.mitrakoff.self.tommypush;

import com.mitrakoff.self.tommypush.comparer.LessComparer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Usage:   java -jar tommypush.jar firebase.json from to date price\nExample: java -jar tommypush.jar /home/user1/firebase-adminsdk.json LED HKT 2023-12-03 35000");
            System.exit(1);
        }

        final var firebaseConfPath = args[0];
        final var from = args[1];
        final var to = args[2];
        final var date = LocalDate.parse(args[3]);
        final var desiredAviasalesRate = Double.parseDouble(args[4]);

        // iPhone X
        final var fcmTokenX = "e5BOi9E7Gk9JhyKDCP2bNy:APA91bHLj9FoQRZKfHH0UGMA8ChP1TuZ7R1abYpkTv65arx__oUsBZfFyKRA3yQ7VrEJCFDOGLb5NKvuICAAiNEXePzvRm-Ig1FkIxFjAuvZckPmiixlruZxo8p5KnQ9XLe2O2ZttlGQ";

        final var firebase = new FirebaseHelper(firebaseConfPath);
        final var aviasalesPath = String.format("https://lyssa.aviasales.ru/price_matrix?origin_iata=%s&destination_iata=%s&depart_start=%s&depart_range=0", from, to, date);

        final List<Checker> checkers = List.of(
            //new Checker("USD", "GET", usdToRubPath, Optional.empty(), "marketdata.data[0][8]", new GreaterComparer(), desiredUsdRate, firebase, List.of(fcmTokenX))
            new Checker("Aviasales", "GET", aviasalesPath, Optional.empty(), "prices[0].value", new LessComparer(), desiredAviasalesRate, firebase, List.of(fcmTokenX))
        );

        checkers.forEach(Thread::start);
    }
}
