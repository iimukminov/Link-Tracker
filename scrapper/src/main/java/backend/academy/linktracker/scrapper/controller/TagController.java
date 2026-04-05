package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @PostMapping("/{name}")
    public void createTag(@PathVariable("name") String name) {
        tagService.create(name);
    }

    @GetMapping
    public List<String> getAllTags() {
        return tagService.findAll();
    }

    @DeleteMapping("/{name}")
    public void deleteTag(@PathVariable("name") String name) {
        tagService.deleteByName(name);
    }

    @PutMapping("/{oldName}")
    public void renameTag(@PathVariable("oldName") String oldName, @RequestParam("newName") String newName) {
        tagService.rename(oldName, newName);
    }
}
