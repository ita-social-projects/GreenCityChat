package greencity.mapping;

import greencity.dto.ChatRoomDto;
import greencity.entity.ChatRoom;
import greencity.entity.Participant;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class ChatRoomMapper extends AbstractConverter<ChatRoomDto, ChatRoom> {
    @Override
    protected ChatRoom convert(ChatRoomDto chatRoomDto) {
        return ChatRoom.builder()
            .id(chatRoomDto.getId())
            .name(chatRoomDto.getName())
            .owner(Participant.builder().id(chatRoomDto.getOwnerId()).build())
            .type(chatRoomDto.getChatType())
            .chatStatus(chatRoomDto.getChatStatus())
            .tariffId(chatRoomDto.getTariffId())
            .participants(chatRoomDto.getParticipants().stream()
                .map(participant -> Participant.builder()
                    .id(participant.getId())
                    .build())
                .collect(Collectors.toSet()))
            .logo(chatRoomDto.getLogo())
            .build();
    }
}
