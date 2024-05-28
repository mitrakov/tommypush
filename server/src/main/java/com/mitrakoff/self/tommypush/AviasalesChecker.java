package com.mitrakoff.self.tommypush;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Duration;
import java.time.LocalDate;
import static java.time.temporal.ChronoUnit.DAYS;

public class AviasalesChecker implements Checker2 {
    final protected String formatMessage;

    public AviasalesChecker(String formatMessage) {
        this.formatMessage = formatMessage;
    }

    @Override
    public String handleJson(JsonNode node) {
        double minPrice = Integer.MAX_VALUE;
        LocalDate minDepart = LocalDate.MAX;
        LocalDate minReturn = LocalDate.MAX;

        final var prices = node.get("data").get("price_chart_v3").get("prices");
        for (final JsonNode obj : prices) {
            final var price = obj.get("price");
            final var departDate = LocalDate.parse(price.get("depart_date").asText());
            final var returnDate = LocalDate.parse(price.get("return_date").asText());
            final var days = Duration.ofDays(DAYS.between(departDate, returnDate)).toDays();
            if (8 <= days && days <= 12) {
                final var value = price.get("value").asDouble();
                if (value < minPrice) {
                    minPrice = value;
                    minDepart = departDate;
                    minReturn = returnDate;
                }
            }
        }

        return String.format(formatMessage, minPrice, minDepart, minReturn);
    }
}
