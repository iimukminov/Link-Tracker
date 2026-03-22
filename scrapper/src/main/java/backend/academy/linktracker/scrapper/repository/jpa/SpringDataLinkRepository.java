package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.LinkEntity;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLinkRepository extends JpaRepository<LinkEntity, Long> {
    Optional<LinkEntity> findByUrl(String url);

    List<LinkEntity> findAllByChatsId(Long chatId, Pageable pageable);

    List<LinkEntity> findByLastUpdateBefore(OffsetDateTime time, Pageable pageable);
}
