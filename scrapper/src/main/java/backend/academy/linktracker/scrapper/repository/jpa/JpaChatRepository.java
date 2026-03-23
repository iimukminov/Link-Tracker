package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatEntity;
import backend.academy.linktracker.scrapper.exceptions.ChatAlreadyRegisteredException;
import backend.academy.linktracker.scrapper.exceptions.ChatNotFoundException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "JPA")
@RequiredArgsConstructor
public class JpaChatRepository implements ChatRepository {

    private final SpringDataChatRepository chatRepository;

    @Override
    @Transactional
    public void save(long chatId) {
        if (chatRepository.existsById(chatId)) {
            throw new ChatAlreadyRegisteredException("Chat with ID " + chatId + " already exists");
        }
        ChatEntity chat = new ChatEntity();
        chat.setId(chatId);
        chatRepository.save(chat);
    }

    @Override
    @Transactional
    public void deleteById(long chatId) {
        if (!chatRepository.existsById(chatId)) {
            throw new ChatNotFoundException("Chat with ID " + chatId + " not found");
        }
        chatRepository.deleteById(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(long chatId) {
        return chatRepository.existsById(chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findAllByLinkId(long linkId) {
        return chatRepository.findAllByLinksId(linkId).stream()
                .map(ChatEntity::getId)
                .toList();
    }
}
