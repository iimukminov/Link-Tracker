package backend.academy.linktracker.scrapper.repository;

import java.util.List;

public interface TagRepository {
    void save(String name);

    void deleteByName(String name);

    List<String> findAll();

    void rename(String oldName, String newName);
}
