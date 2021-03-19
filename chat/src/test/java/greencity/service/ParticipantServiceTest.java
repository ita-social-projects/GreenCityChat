package greencity.service;

import greencity.dto.ParticipantDto;
import greencity.entity.Participant;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.UserNotFoundException;
import greencity.repository.ParticipantRepo;
import greencity.service.impl.ParticipantServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {
    @InjectMocks
    private ParticipantServiceImpl participantServiceImpl;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ParticipantRepo participantRepo;

    private final String email = "test.artur@mail.com";
    Participant expected;
    ParticipantDto expectedDto;
    List<Participant> expectedList;
    List<ParticipantDto> expectedDtoList;

    @BeforeEach
    void init() {
        expected = Participant.builder()
            .id(1L)
            .name("artur")
            .email(email)
            .profilePicture(null)
            .userStatus(UserStatus.ACTIVATED)
            .build();
        expectedDto = ParticipantDto.builder()
            .id(1L)
            .name("artur")
            .email(email)
            .profilePicture(null)
            .userStatus(UserStatus.ACTIVATED)
            .build();
        expectedList = new ArrayList<>();
        expectedList.add(expected);
        expectedDtoList = new ArrayList<>();
        expectedDtoList.add(expectedDto);
    }

    @Test
    void findByEmail() {
        when(participantRepo.findNotDeactivatedByEmail(email)).thenReturn(Optional.of(expected));
        Optional<Participant> actual = Optional.ofNullable(participantServiceImpl.findByEmail(email));
        assertEquals(Optional.of(expected), actual);
    }

    @Test
    void findByEmailException() {
        when(participantRepo.findNotDeactivatedByEmail(email)).thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class, () -> {
            participantServiceImpl.findByEmail(email);
        });
    }

    @Test
    void findById() {
        when(participantRepo.findById(1L)).thenReturn(Optional.of(expected));
        Optional<Participant> actual = Optional.ofNullable(participantServiceImpl.findById(1L));
        assertEquals(Optional.of(expected), actual);
    }

    @Test
    void findByIdException() {
        when(participantRepo.findById(1L)).thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class, () -> {
            participantServiceImpl.findById(1L);
        });
    }

    @Test
    void getCurrentParticipantByEmail() {
        when(participantRepo.findNotDeactivatedByEmail(email)).thenReturn(Optional.of(expected));
        when(modelMapper.map(expected, ParticipantDto.class)).thenReturn(expectedDto);
        ParticipantDto actualDto = participantServiceImpl.getCurrentParticipantByEmail(email);
        assertEquals(expectedDto, actualDto);
    }

    @Test
    void getCurrentParticipantByEmailException() {
        when(participantRepo.findNotDeactivatedByEmail(email)).thenReturn(Optional.of(expected));
        when(modelMapper.map(expected, ParticipantDto.class)).thenThrow(UserNotFoundException.class);
        assertThrows(UserNotFoundException.class, () -> {
            participantServiceImpl.getCurrentParticipantByEmail(email);
        });
    }

    @Test
    void findAllExceptCurrentUser() {
        when(participantRepo.findAllExceptCurrentUser(email)).thenReturn(expectedList);
        when(modelMapper.map(expectedList, new TypeToken<List<ParticipantDto>>() {
        }.getType())).thenReturn(expectedDtoList);
        List<ParticipantDto> actual = participantServiceImpl.findAllExceptCurrentUser(email);
        assertEquals(expectedDtoList, actual);
    }

    @Test
    void findAllParticipantsByQuery() {
        when(participantRepo.findAllParticipantsByQuery("query", "tomas")).thenReturn(expectedList);
        when(modelMapper.map(expectedList, new TypeToken<List<ParticipantDto>>() {
        }.getType())).thenReturn(expectedDtoList);
        List<ParticipantDto> actual = participantServiceImpl.findAllParticipantsByQuery("query", "tomas");
        assertEquals(expectedDtoList, actual);
    }
}