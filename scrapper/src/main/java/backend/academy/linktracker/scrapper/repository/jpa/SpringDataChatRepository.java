package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataChatRepository extends JpaRepository<ChatEntity, Long> {
    List<ChatEntity> findAllByLinksId(Long linkId);
}
