package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.repository.TagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public void create(String name) {
        tagRepository.save(name);
    }

    @Transactional(readOnly = true)
    public List<String> findAll() {
        return tagRepository.findAll();
    }

    @Transactional
    public void deleteByName(String name) {
        tagRepository.deleteByName(name);
    }

    @Transactional
    public void rename(String oldName, String newName) {
        tagRepository.rename(oldName, newName);
    }
}
