package backend.academy.linktracker.scrapper.repository.jpa;

import backend.academy.linktracker.scrapper.entity.TagEntity;
import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@ConditionalOnProperty(prefix = "app.database", name = "access-type", havingValue = "JPA")
@RequiredArgsConstructor
public class JpaTagRepository implements TagRepository {

    private final SpringDataTagRepository tagRepository;

    @Override
    @Transactional
    public void save(String name) {
        if (tagRepository.findByName(name).isEmpty()) {
            TagEntity tag = new TagEntity();
            tag.setName(name);
            tagRepository.save(tag);
        }
    }

    @Override
    @Transactional
    public void deleteByName(String name) {
        tagRepository.deleteByName(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAll() {
        return tagRepository.findAll().stream().map(TagEntity::getName).toList();
    }

    @Override
    @Transactional
    public void rename(String oldName, String newName) {
        tagRepository.findByName(oldName).ifPresent(tag -> {
            tag.setName(newName);
            tagRepository.save(tag);
        });
    }
}
