package greencity.service.impl;

import greencity.dto.LanguageDto;
import greencity.entity.Language;
import greencity.exception.exceptions.LanguageNotFoundException;
import greencity.repository.LanguageRepo;
import greencity.service.LanguageService;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class LanguageServiceImpl implements LanguageService {
    private final LanguageRepo languageRepo;
    private final ModelMapper modelMapper;
    private HttpServletRequest request;

    /**
     * Implementation of {@link LanguageService}.
     *
     * @author Oleh Kopylchak
     * @author Vitaliy Dzen
     */
    @Autowired
    public LanguageServiceImpl(
        LanguageRepo languageRepo,
        @Lazy ModelMapper modelMapper, HttpServletRequest request) {
        this.languageRepo = languageRepo;
        this.modelMapper = modelMapper;
        this.request = request;
    }

    @Override
    public List<LanguageDto> getAllLanguages() {
        return modelMapper.map(languageRepo.findAll(), new TypeToken<List<LanguageDto>>() {
        }.getType());
    }

    @Override
    public String extractLanguageCodeFromRequest() {
        String languageCode = request.getParameter("language");

        if (languageCode == null) {
            return "en";
        }

        return languageCode;
    }

    @Override
    public LanguageDto findByCode(String code) {
        Language language = languageRepo.findByCode(code)
            .orElseThrow(() -> new LanguageNotFoundException("Given language code is not supported."));
        return modelMapper.map(language, LanguageDto.class);
    }

    @Override
    public List<String> findAllLanguageCodes() {
        return languageRepo.findAllLanguageCodes();
    }
}
