package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.exceptions.ChatAlreadyRegisteredException;
import backend.academy.linktracker.scrapper.exceptions.ChatNotFoundException;
import backend.academy.linktracker.scrapper.properties.ScrapperMessages;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TgChatService {

    private final ChatRepository chatRepository;
    private final ScrapperMessages scrapperMessages;

    @Transactional
    public void registerChat(Long id) {
        if (chatRepository.existsById(id)) {
            throw new ChatAlreadyRegisteredException(scrapperMessages.getErrors().getChatAlreadyRegistered());
        }
        chatRepository.save(id);
    }

    @Transactional
    public void deleteChat(Long id) {
        if (!chatRepository.existsById(id)) {
            throw new ChatNotFoundException(scrapperMessages.getErrors().getChatNotFound());
        }
        chatRepository.deleteById(id);
    }
}
