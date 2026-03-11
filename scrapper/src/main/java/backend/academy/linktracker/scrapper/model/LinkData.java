package backend.academy.linktracker.scrapper.model;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode(of = "url")
public class LinkData {
    private Long id;
    private URI url;
    private OffsetDateTime lastUpdate;
    private List<String> tags;
    private List<String> filters;
}
