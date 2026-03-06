package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.constants.UserStates;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserStateService {
    private final Map<Long, UserStates> chatStates = new ConcurrentHashMap<>();

    public UserStates getState(long chatId) {
        return chatStates.getOrDefault(chatId, UserStates.IDLE);
    }

    public void setState(long chatId, UserStates state) {
        log.info("User {} changed state to {}", chatId, state);
        chatStates.put(chatId, state);
    }

    public void clearState(long chatId) {
        log.info("User {} cleared state", chatId);
        chatStates.remove(chatId);
    }

    public boolean isIdle(long chatId) {
        return !chatStates.containsKey(chatId) || UserStates.IDLE.equals(chatStates.get(chatId));
    }
}
