package greencity.entity;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.*;

import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private ChatRoom room;

    @ManyToOne
    private Participant sender;

    @Column
    private String content;
    private ZonedDateTime createDate;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<UnreadMessage> unreadMessages;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_url")
    private String fileUrl;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
        name = "message_like",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "participant_id"))
    private Set<Participant> likes = new HashSet<>();
}
