package greencity.entity;

import java.time.ZonedDateTime;
import java.util.List;
import javax.persistence.*;

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
    private ZonedDateTime createDate;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<UnreadMessage> unreadMessages;
}
