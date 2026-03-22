package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.TagEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataTagRepository extends JpaRepository<TagEntity, Long> {
    Optional<TagEntity> findByName(String name);

    void deleteByName(String name);
}
