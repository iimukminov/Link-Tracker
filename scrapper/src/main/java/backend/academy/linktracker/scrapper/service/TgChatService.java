package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TgChatService {

    private final ChatRepository chatRepository;

    @Transactional
    public void registerChat(Long id) {
        chatRepository.save(id);
    }

    @Transactional
    public void deleteChat(Long id) {
        chatRepository.deleteById(id);
    }
}
