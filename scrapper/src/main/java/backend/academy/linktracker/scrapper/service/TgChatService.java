package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.repository.InMemoryScrapperRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TgChatService {

    private final InMemoryScrapperRepository repository;

    public void registerChat(Long id) {
        repository.addChat(id);
    }

    public void deleteChat(Long id) {
        repository.removeChat(id);
    }
}
