package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.LinkEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LinkJpaRepository extends JpaRepository<LinkEntity, Long> {
    Optional<LinkEntity> findByUrl(String url);

    @Query(value = """
    SELECT l.* FROM link l
    JOIN link_chat lc ON l.id = lc.link_id
    WHERE lc.chat_id = :chatId
    LIMIT :limit OFFSET :offset
    """, nativeQuery = true)
    List<LinkEntity> findAllByChatsId(
            @Param("chatId") Long chatId, @Param("limit") int limit, @Param("offset") int offset);

    List<LinkEntity> findByLastUpdateBefore(OffsetDateTime time, Pageable pageable);

    List<LinkEntity> findByLastCheckAtBefore(OffsetDateTime time, Pageable pageable);

    boolean existsByChatsIdAndUrl(Long chatId, String url);

    @Modifying
    @Query(value = "UPDATE link SET last_check_at = :time WHERE id = :id", nativeQuery = true)
    void updateLastCheck(@Param("id") Long id, @Param("time") OffsetDateTime time);

    @Modifying
    @Query(value = "UPDATE link SET last_update = :time WHERE id = :id", nativeQuery = true)
    void updateLastUpdate(@Param("id") Long id, @Param("time") OffsetDateTime time);
}
