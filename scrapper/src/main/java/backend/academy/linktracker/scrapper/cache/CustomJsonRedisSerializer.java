package backend.academy.linktracker.scrapper.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

@RequiredArgsConstructor
public class CustomJsonRedisSerializer implements RedisSerializer<Object> {

    private final ObjectMapper mapper;

    @Override
    public byte[] serialize(Object value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        try {
            return mapper.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Ошибка сериализации в JSON", e);
        }
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            return mapper.readValue(bytes, Object.class);
        } catch (IOException e) {
            throw new SerializationException("Ошибка десериализации из JSON", e);
        }
    }
}
