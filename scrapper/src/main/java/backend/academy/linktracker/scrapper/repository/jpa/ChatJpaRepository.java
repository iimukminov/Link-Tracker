package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.ChatEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatJpaRepository extends JpaRepository<ChatEntity, Long> {
    @Query(value = "SELECT chat_id FROM link_chat WHERE link_id = :linkId", nativeQuery = true)
    List<Long> findAllIdsByLinkId(@Param("linkId") Long linkId);
}
