package greencity.entity;

import java.time.ZonedDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
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

    @Column
    private ZonedDateTime createDate;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<UnreadMessage> unreadMessages;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_url")
    private String fileUrl;
}
