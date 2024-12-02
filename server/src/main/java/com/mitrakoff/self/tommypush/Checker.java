package com.mitrakoff.self.tommypush;

import com.fasterxml.jackson.databind.JsonNode;

public interface Checker {
    String handleJson(JsonNode node);
}
