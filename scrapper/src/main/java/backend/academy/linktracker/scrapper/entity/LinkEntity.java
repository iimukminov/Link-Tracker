package backend.academy.linktracker.scrapper.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "link")
@Getter
@Setter
public class LinkEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 2048)
    private String url;

    @Column(name = "last_update", nullable = false)
    private OffsetDateTime lastUpdate;

    @ManyToMany(mappedBy = "links")
    private Set<ChatEntity> chats = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "link_tag",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<TagEntity> tags = new HashSet<>();
}
