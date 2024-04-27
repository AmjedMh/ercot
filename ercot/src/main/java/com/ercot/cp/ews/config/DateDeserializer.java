package com.ercot.cp.ews.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.log4j.Log4j2;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Log4j2
public class DateDeserializer extends JsonDeserializer<LocalDateTime> {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        try {
            return LocalDateTime.parse(jsonParser.getText(), formatter);

        } catch (Exception exception) {
            log.error("Exception occurred while parsing date: {}", exception.getMessage(), exception);
        }
        return null;
    }
}