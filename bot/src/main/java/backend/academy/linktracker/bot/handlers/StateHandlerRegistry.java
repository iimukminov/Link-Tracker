package backend.academy.linktracker.bot.handlers;

import backend.academy.linktracker.bot.constants.UserState;
import backend.academy.linktracker.bot.handlers.impl.DefaultStateHandler;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class StateHandlerRegistry {
    private final Map<UserState, StateHandler> handlers;
    private final StateHandler defaultHandler;

    public StateHandlerRegistry(List<StateHandler> handlers, DefaultStateHandler defaultHandler) {
        this.handlers = handlers.stream()
                .filter(handler -> handler.getSupportedState() != UserState.IDLE)
                .collect(Collectors.toConcurrentMap(StateHandler::getSupportedState, handler -> handler));
        this.defaultHandler = defaultHandler;
    }

    public StateHandler getHandler(UserState userState) {
        return handlers.getOrDefault(userState, defaultHandler);
    }
}
