package com.mitrakoff.self.tommypush;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.LocalDate;

public class AviasalesSimple implements Checker {
    final protected String formatMessage;

    public AviasalesSimple(String formatMessage) {
        this.formatMessage = formatMessage;
    }

    @Override
    public String handleJson(JsonNode node) {
        final var cheapest = node.get("data").get("best_prices_v2").get("cheapest_direct");
        final var departDate = LocalDate.parse(cheapest.get("depart_date").asText());
        final var returnDate = LocalDate.parse(cheapest.get("return_date").asText());
        final var value = cheapest.get("value").asInt();

        return String.format(formatMessage, value, departDate, returnDate);
    }
}
