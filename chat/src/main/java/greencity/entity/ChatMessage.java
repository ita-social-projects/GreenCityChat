package greencity.entity;

import java.time.ZonedDateTime;
import java.util.List;
import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column
    private String fileName;

    @Column
    private String fileType;

    @Column
    private String fileUrl;

    @OneToMany(mappedBy = "message")
    private List<UnreadMessage> unreadMessages;
}
