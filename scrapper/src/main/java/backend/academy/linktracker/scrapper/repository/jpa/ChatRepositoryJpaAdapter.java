package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatEntity;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "ORM")
@RequiredArgsConstructor
public class ChatRepositoryJpaAdapter implements ChatRepository {

    private final ChatJpaRepository chatRepository;

    @Override
    @Transactional
    public void save(long chatId) {
        ChatEntity chat = new ChatEntity();
        chat.setId(chatId);
        chatRepository.save(chat);
    }

    @Override
    @Transactional
    public void deleteById(long chatId) {
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
        return chatRepository.findAllIdsByLinkId(linkId);
    }
}
