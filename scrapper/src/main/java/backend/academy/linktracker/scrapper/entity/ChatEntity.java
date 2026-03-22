package backend.academy.linktracker.scrapper.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "chat")
@Getter
@Setter
public class ChatEntity {
    @Id
    private Long id;

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "link_chat",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "link_id"))
    private Set<LinkEntity> links = new HashSet<>();
}
