package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.constants.UserState;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserStateService {
    private final Map<Long, UserState> chatStates = new ConcurrentHashMap<>();
    private final Map<Long, Map<String, String>> tempData = new ConcurrentHashMap<>();

    public UserState getState(long chatId) {
        return chatStates.getOrDefault(chatId, UserState.IDLE);
    }

    public void setState(long chatId, UserState state) {
        log.atInfo()
                .setMessage("User state changed")
                .addKeyValue("chatId", chatId)
                .addKeyValue("newState", state)
                .log();
        chatStates.put(chatId, state);
    }

    public void clearState(long chatId) {
        log.atInfo()
                .setMessage("User state cleared")
                .addKeyValue("chatId", chatId)
                .log();
        chatStates.remove(chatId);
    }

    public String getTempData(long chatId, String key) {
        return tempData.getOrDefault(chatId, Map.of()).get(key);
    }

    public void setTempData(long chatId, String key, String value) {
        tempData.computeIfAbsent(chatId, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    public void clearTempData(long chatId) {
        tempData.remove(chatId);
    }
}
