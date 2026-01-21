package server.http.servlets;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Gson adapter cho LocalDateTime
 * Chuyển đổi LocalDateTime sang ISO-8601 format
 */
public class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>,
        JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public JsonElement serialize(LocalDateTime dateTime, Type typeOfSrc,
            JsonSerializationContext context) {
        return new JsonPrimitive(dateTime.format(FORMATTER));
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context)
            throws JsonParseException {
        return LocalDateTime.parse(json.getAsString(), FORMATTER);
    }
}