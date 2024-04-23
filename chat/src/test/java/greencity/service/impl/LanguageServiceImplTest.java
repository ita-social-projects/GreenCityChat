package greencity.service.impl;

import greencity.dto.LanguageDto;
import greencity.entity.Language;
import greencity.exception.exceptions.LanguageNotFoundException;
import greencity.repository.LanguageRepo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import org.modelmapper.TypeToken;

@ExtendWith(MockitoExtension.class)
public class LanguageServiceImplTest {
    @InjectMocks
    private LanguageServiceImpl languageService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private LanguageRepo languageRepo;
    @Mock
    private HttpServletRequest request;

    private Language expected;
    private LanguageDto expectedDto;
    private List<String> expectedList;
    private List<LanguageDto> expectedDtoList;

    @BeforeEach
    void init() {
        expected = new Language();
        expected.setId(1L);
        expected.setCode("en");
        expectedDto = LanguageDto.builder()
            .id(1L)
            .code("en")
            .build();
        expectedList = new ArrayList<>();
        expectedList.add("en");
        expectedDtoList = new ArrayList<>();
        expectedDtoList.add(expectedDto);
    }

    @Test
    void getAllLanguages() {
        when(languageRepo.findAll()).thenReturn(Arrays.asList(expected));
        when(modelMapper.map(languageRepo.findAll(), new TypeToken<List<LanguageDto>>() {
        }.getType())).thenReturn(expectedDtoList);
        List<LanguageDto> actual = languageService.getAllLanguages();
        assertEquals(expectedDtoList, actual);
    }

    @Test
    void extractLanguageCodeFromRequestTest() {
        when(request.getParameter("language")).thenReturn("ua");
        String actual = languageService.extractLanguageCodeFromRequest();
        assertEquals("ua", actual);
    }

    @Test
    void extractLanguageCodeFromRequestTestWithNull() {
        when(request.getParameter("language")).thenReturn(null);
        String actual = languageService.extractLanguageCodeFromRequest();
        assertEquals("en", actual);
    }

    @Test
    void findByCodeTest() {
        String code = "en";
        when(languageRepo.findByCode(code)).thenReturn(Optional.ofNullable(expected));
        when(modelMapper.map(languageRepo.findByCode(code).orElse(null), LanguageDto.class)).thenReturn(expectedDto);
        LanguageDto actual = languageService.findByCode(code);
        assertEquals(expectedDto, actual);
    }

    @Test
    void findByCodeTestException() {
        String code = "en";
        when(languageRepo.findByCode(code)).thenThrow(LanguageNotFoundException.class);
        assertThrows(LanguageNotFoundException.class, () -> {
            languageService.findByCode(code);
        });
    }

    @Test
    void findAllLanguageCodesTest() {
        when(languageRepo.findAllLanguageCodes()).thenReturn(expectedList);
        List<String> actual = languageService.findAllLanguageCodes();
        assertEquals(expectedList, actual);
    }
}
