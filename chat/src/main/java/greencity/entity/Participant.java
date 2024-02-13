package greencity.entity;

import greencity.enums.Role;
import greencity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "users")
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    @OneToMany(mappedBy = "owner")
    private List<ChatRoom> rooms;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Enumerated(value = EnumType.ORDINAL)
    private UserStatus userStatus;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, name = "role")
    private Role role;

    @OneToMany(mappedBy = "participant")
    private List<UnreadMessage> unreadMessages;
}
